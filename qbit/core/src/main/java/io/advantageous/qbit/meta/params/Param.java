/*
 * Copyright (c) 2015. Rick Hightower, Geoff Chandler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 */
package io.advantageous.qbit.meta.params;


/**
 * Holds meta data about a body where some param named or positional represents a single argument to a method.
 */
public class Param {

    private final boolean required;
    private final Object defaultValue;
    private final ParamType paramType;

    public Param(boolean required, Object defaultValue, ParamType paramType) {
        this.required = required;
        this.defaultValue = defaultValue;
        this.paramType = paramType;
    }

    public static Param[] params(final Param... params) {
        return params;
    }

    public static HeaderParam headParamRequired(final String name) {
        return new HeaderParam(true, name, null);
    }

    public static HeaderParam headParam(final String name) {
        return new HeaderParam(false, name, null);
    }

    public static HeaderParam headParam(final String name, Object defaultValue) {
        return new HeaderParam(false, name, defaultValue);
    }

    public static RequestParam requestParamRequired(final String name) {
        return new RequestParam(true, name, null);
    }

    public static RequestParam requestParam(final String name) {
        return new RequestParam(false, name, null);
    }

    public static RequestParam requestParam(final String name, Object defaultValue) {
        return new RequestParam(false, name, defaultValue);
    }

    public static URINamedParam pathParamRequired(final String name, final int indexIntoURI) {
        return new URINamedParam(true, name, null, indexIntoURI);
    }

    public static URINamedParam pathParam(final String name, final int indexIntoURI) {
        return new URINamedParam(false, name, null, indexIntoURI);
    }

    public static URINamedParam pathParam(final String name, final int indexIntoURI, Object defaultValue) {
        return new URINamedParam(false, name, defaultValue, indexIntoURI);
    }

    public static URIPositionalParam pathParamRequired(final int pos, final int indexIntoURI) {
        return new URIPositionalParam(true, pos, null, indexIntoURI);
    }

    public static URIPositionalParam pathParam(final int pos, final int indexIntoURI) {
        return new URIPositionalParam(false, pos, null, indexIntoURI);
    }

    public static URIPositionalParam pathParam(final int pos, final int indexIntoURI, Object defaultValue) {
        return new URIPositionalParam(false, pos, defaultValue, indexIntoURI);
    }

    public static BodyParam bodyParamRequired() {
        return new BodyParam(true, null);
    }

    public static BodyParam bodyParam() {
        return new BodyParam(false, null);
    }

    public static BodyArrayParam bodyParamRequired(final int pos) {
        return new BodyArrayParam(true, pos, null);
    }

    public static BodyArrayParam bodyParam(final int pos) {
        return new BodyArrayParam(false, pos, null);
    }

    public static BodyArrayParam bodyParam(final int pos, Object defaultValue) {
        return new BodyArrayParam(false, pos, defaultValue);
    }

    public boolean isRequired() {
        return required;
    }

    public ParamType getParamType() {
        return paramType;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

}
