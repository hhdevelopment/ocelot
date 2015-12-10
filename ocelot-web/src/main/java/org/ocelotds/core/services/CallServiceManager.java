/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package org.ocelotds.core.services;

import org.ocelotds.configuration.OcelotConfiguration;
import org.ocelotds.annotations.DataService;
import org.ocelotds.annotations.JsCacheResult;
import org.ocelotds.messaging.Fault;
import org.ocelotds.messaging.MessageFromClient;
import org.ocelotds.messaging.MessageToClient;
import org.ocelotds.resolvers.DataServiceResolverIdLitteral;
import org.ocelotds.spi.DataServiceException;
import org.ocelotds.spi.IDataServiceResolver;
import org.ocelotds.spi.Scope;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.websocket.Session;
import org.ocelotds.Constants;
import org.ocelotds.core.CacheManager;
import org.ocelotds.annotations.OcelotLogger;
import org.ocelotds.marshalling.annotations.JsonMarshaller;
import org.ocelotds.marshalling.exceptions.JsonUnmarshallingException;
import org.slf4j.Logger;

/**
 * Abstract class of OcelotDataService
 *
 * @author hhfrancois
 */
public class CallServiceManager implements CallService {

	@Inject
	@OcelotLogger
	private Logger logger;

	@Inject
	@Any
	private Instance<IDataServiceResolver> resolvers;

	@Inject
	private OcelotConfiguration configuration;

	@Inject
	private CacheManager cacheManager;
	
	@Inject
	private IArgumentConvertor argumentsServices;

	IDataServiceResolver getResolver(String type) {
		return resolvers.select(new DataServiceResolverIdLitteral(type)).get();
	}

	/**
	 * Get pertinent method and fill the argument list from message arguments
	 *
	 * @param dsClass
	 * @param message
	 * @param arguments
	 * @return
	 * @throws java.lang.NoSuchMethodException
	 */
	Method getMethodFromDataService(final Class dsClass, final MessageFromClient message, Object[] arguments) throws NoSuchMethodException {
		logger.debug("Try to find method {} on class {}", message.getOperation(), dsClass);
		List<String> parameters = message.getParameters();
		for (Method method : dsClass.getMethods()) {
			if (method.getName().equals(message.getOperation()) && method.getParameterTypes().length == parameters.size()) {
				logger.debug("Process method {}", method.getName());
				try {
					Type[] paramTypes = method.getGenericParameterTypes();
					Annotation[][] parametersAnnotations = method.getParameterAnnotations();
					int idx = 0;
					for (Type paramType : paramTypes) {
						logger.debug("Try to convert argument ({}) {} : {}.", new Object[]{idx, paramType.toString(), parameters.get(idx)});
						arguments[idx] = argumentsServices.convertJsonToJava(parameters.get(idx), paramType, parametersAnnotations[idx]);
						idx++;
					}
					logger.debug("Method {}.{} with good signature found.", dsClass, message.getOperation());
					return method;
				} catch (JsonUnmarshallingException | IllegalArgumentException iae) {
					logger.debug("Method {}.{} not found. Some arguments didn't match. {}.", new Object[]{dsClass, message.getOperation(), iae.getMessage()});
				}
			}
		}
		throw new NoSuchMethodException(dsClass + "." + message.getOperation());
	}

	/**
	 * Get Dataservice, store dataservice in session if session scope.<br>
	 *
	 * @param client
	 * @param cls
	 * @return
	 * @throws DataServiceException
	 */
	Object getDataService(Session client, Class cls) throws DataServiceException {
		String dataServiceClassName = cls.getName();
		logger.debug("Looking for dataservice : {}", dataServiceClassName);
		if (cls.isAnnotationPresent(DataService.class)) {
			try {
				return _getDataService(client, cls);
			} catch (Exception e) {
				throw new DataServiceException(dataServiceClassName, e);
			}
		} else {
			throw new DataServiceException(dataServiceClassName);
		}
	}

	/**
	 * Get Dataservice, store dataservice in session if session scope.<br>
	 *
	 * @param client
	 * @param cls
	 * @return
	 * @throws DataServiceException
	 */
	Object _getDataService(Session client, Class cls) throws Exception {
		String dataServiceClassName = cls.getName();
		DataService dataServiceAnno = (DataService) cls.getAnnotation(DataService.class);
		IDataServiceResolver resolver = getResolver(dataServiceAnno.resolver());
		Scope scope = resolver.getScope(cls);
		Object dataService = null;
		Map sessionBeans = (Map) client.getUserProperties().get(Constants.SESSION_BEANS);
		logger.debug("{} : scope : {}", dataServiceClassName, scope);
		if (scope.equals(Scope.SESSION)) {
			dataService = sessionBeans.get(dataServiceClassName);
			logger.debug("{} : scope : session is in session : {}", dataServiceClassName, (dataService != null));
		}
		if (dataService == null) {
			dataService = resolver.resolveDataService(cls);
			if (scope.equals(Scope.SESSION)) {
				logger.debug("Store {} scope session in session", dataServiceClassName);
				sessionBeans.put(dataServiceClassName, dataService);
			}
		}
		return dataService;
	}

	/**
	 * Build and send response messages after call request
	 *
	 * @param message
	 * @param client
	 */
	@Override
	public void sendMessageToClient(MessageFromClient message, Session client) {
		MessageToClient messageToClient = new MessageToClient();
		messageToClient.setId(message.getId());
		try {
			Class cls = Class.forName(message.getDataService());
			Object dataService = this.getDataService(client, cls);
			logger.debug("Process message {}", message);
			int nbParam = message.getParameters().size();
			Object[] arguments = new Object[nbParam];
			Method method = this.getMethodFromDataService(cls, message, arguments);
			messageToClient.setResult(method.invoke(dataService, arguments));
			if (method.isAnnotationPresent(JsonMarshaller.class)) {
				JsonMarshaller jm = method.getAnnotation(JsonMarshaller.class);
				Class<? extends org.ocelotds.marshalling.JsonMarshaller> marshallerCls = jm.value();
				org.ocelotds.marshalling.JsonMarshaller marshaller = marshallerCls.newInstance();
				String json = marshaller.toJson(messageToClient.getResponse());
				messageToClient.setJson(json);
			}
			try {
				Method nonProxiedMethod = this.getNonProxiedMethod(cls, method.getName(), method.getParameterTypes());
				if (cacheManager.isJsCached(nonProxiedMethod)) {
					JsCacheResult jcr = nonProxiedMethod.getAnnotation(JsCacheResult.class);
					messageToClient.setDeadline(cacheManager.getJsCacheResultDeadline(jcr));
				}
				cacheManager.processCleanCacheAnnotations(nonProxiedMethod, message.getParameterNames(), message.getParameters());
				if (logger.isDebugEnabled()) {
					logger.debug("Method {} proceed messageToClient : {}.", method.getName(), messageToClient.toJson());
				}
			} catch (NoSuchMethodException ex) {
				logger.error("Fail to process extra annotations (JsCacheResult, JsCacheRemove) for method : " + method.getName(), ex);
			}
		} catch (InvocationTargetException ex) {
			messageToClient.setFault(buildFault(ex.getCause()));
		} catch (Throwable ex) {
			messageToClient.setFault(buildFault(ex));
		}
		client.getAsyncRemote().sendObject(messageToClient);
	}

	Fault buildFault(Throwable ex) {
		Fault fault;
		int stacktracelength = configuration.getStacktracelength();
		if (stacktracelength == 0 || logger.isDebugEnabled()) {
			logger.error("Invocation failed", ex);
		}
		fault = new Fault(ex, stacktracelength);
		return fault;
	}

	/**
	 * Get the method on origin class without proxies
	 *
	 * @param cls
	 * @param methodName
	 * @param parameterTypes
	 * @throws NoSuchMethodException
	 * @return
	 */
	Method getNonProxiedMethod(Class cls, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
		return cls.getMethod(methodName, parameterTypes);
	}
}
