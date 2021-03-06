/**
 * Copyright (c) 2009-2010 fluent-builder-generator for Eclipse commiters.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sabre Polska sp. z o.o. - initial implementation during Hackday
 */

package com.sabre.buildergenerator.signatureutils;

public interface SignatureHandler {
    void simpleType(char type) throws ExceptionWrapper;
    void array() throws ExceptionWrapper;
    void typeVariable(String identifier) throws ExceptionWrapper;
    void startResolvedType(String identifier) throws ExceptionWrapper;
    void startUnresolvedType(String identifier) throws ExceptionWrapper;
    void innerType(String identifier) throws ExceptionWrapper;
    void endType() throws ExceptionWrapper;
    void startTypeArguments() throws ExceptionWrapper;
    void endTypeArguments() throws ExceptionWrapper;
    void wildcardAny() throws ExceptionWrapper;
    void wildcardExtends() throws ExceptionWrapper;
    void wildcardSuper() throws ExceptionWrapper;
    void captureOf() throws ExceptionWrapper;
}
