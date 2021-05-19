package io.gatehill.imposter.scripting.groovy.impl;

import groovy.lang.Script;
import io.gatehill.imposter.script.MutableResponseBehaviour;
import io.gatehill.imposter.script.ResponseBehaviourType;
import io.gatehill.imposter.script.ScriptedResponseBehavior;
import io.gatehill.imposter.script.impl.ScriptedResponseBehaviorImpl;

import java.util.Map;

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
public abstract class GroovyResponseBehaviourImpl extends Script implements ScriptedResponseBehavior {
    private final ScriptedResponseBehavior delegate = new ScriptedResponseBehaviorImpl();

    @Override
    public Map<String, String> getResponseHeaders() {
        return delegate.getResponseHeaders();
    }

    @Override
    public ResponseBehaviourType getBehaviourType() {
        return delegate.getBehaviourType();
    }

    @Override
    public String getResponseFile() {
        return delegate.getResponseFile();
    }

    @Override
    public int getStatusCode() {
        return delegate.getStatusCode();
    }

    @Override
    public String getResponseData() {
        return delegate.getResponseData();
    }

    @Override
    public String getExampleName() {
        return delegate.getExampleName();
    }

    @Override
    public MutableResponseBehaviour withHeader(String header, String value) {
        delegate.withHeader(header, value);
        return this;
    }

    @Override
    public MutableResponseBehaviour withStatusCode(int statusCode) {
        delegate.withStatusCode(statusCode);
        return this;
    }

    @Override
    public MutableResponseBehaviour withFile(String responseFile) {
        delegate.withFile(responseFile);
        return this;
    }

    @Override
    public MutableResponseBehaviour withData(String responseData) {
        delegate.withData(responseData);
        return this;
    }

    @Override
    public MutableResponseBehaviour withExampleName(String exampleName) {
        delegate.withExampleName(exampleName);
        return this;
    }

    @Override
    public MutableResponseBehaviour withEmpty() {
        delegate.withEmpty();
        return this;
    }

    @Override
    public MutableResponseBehaviour usingDefaultBehaviour() {
        delegate.usingDefaultBehaviour();
        return this;
    }

    @Override
    public MutableResponseBehaviour immediately() {
        delegate.immediately();
        return this;
    }

    @Override
    public MutableResponseBehaviour respond() {
        delegate.respond();
        return this;
    }

    @Override
    public MutableResponseBehaviour respond(Runnable closure) {
        delegate.respond(closure);
        return this;
    }

    @Override
    public MutableResponseBehaviour and() {
        delegate.and();
        return this;
    }
}