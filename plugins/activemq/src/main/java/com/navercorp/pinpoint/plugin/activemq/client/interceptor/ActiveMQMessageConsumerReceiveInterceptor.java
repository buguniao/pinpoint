package com.navercorp.pinpoint.plugin.activemq.client.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.Scope;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.TargetMethod;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.TargetMethods;
import com.navercorp.pinpoint.plugin.activemq.client.ActiveMQClientConstants;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.JMSException;

/**
 * @author HyunGil Jeong
 */
@Scope(value = ActiveMQClientConstants.ACTIVEMQ_CLIENT_SCOPE)
@TargetMethods({
        @TargetMethod(name = "receive"),
        @TargetMethod(name = "receive", paramTypes = "long"),
        @TargetMethod(name = "receiveNoWait")
})
public class ActiveMQMessageConsumerReceiveInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    public ActiveMQMessageConsumerReceiveInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);
    }

    // These methods may be polled, producing a lot of garbage log.
    // Instead, only log when the method is actually traced.
    @Override
    protected void logBeforeInterceptor(Object target, Object[] args) {
        return;
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        if (isDebug) {
            super.logBeforeInterceptor(target, args);
        }
    }

    // These methods may be polled, producing a lot of garbage log.
    // Instead, only log when the method is actually traced.
    @Override
    protected void logAfterInterceptor(Object target, Object[] args, Object result, Throwable throwable) {
        return;
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            super.logAfterInterceptor(target, args, result, throwable);
        }
        recorder.recordServiceType(ActiveMQClientConstants.ACTIVEMQ_CLIENT_INTERNAL);
        recorder.recordApi(getMethodDescriptor());
        if (throwable != null) {
            recorder.recordException(throwable);
        } else {
            if (result != null) {
                StringBuilder sb = new StringBuilder(result.getClass().getSimpleName());
                try {
                    // should we record other message types as well?
                    if (result instanceof ActiveMQTextMessage) {
                        // could trigger decoding (would it affect the client? if so, we might need to copy first)
                        sb.append("{").append(((ActiveMQTextMessage) result).getText()).append("}");
                    }
                } catch (JMSException e) {
                    // ignore
                }
                recorder.recordAttribute(ActiveMQClientConstants.ACTIVEMQ_MESSAGE, sb.toString());
            }
        }
    }
}