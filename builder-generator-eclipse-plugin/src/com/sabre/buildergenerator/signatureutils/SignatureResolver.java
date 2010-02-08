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

import java.util.Map;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;


public class SignatureResolver {
    /**
     * @param typeSignature
     * @return
     */
    public static String signatureToTypeName(String typeSignature) {
        return SignatureToType.resolveSignature(typeSignature);
    }

    /**
     * QList&lt;QString;&gt;; --> QList;
     * @param signature signature
     * @return signature for type erasure
     */
    private static String getTypeErasure(String signature) {
        return String.valueOf(Signature.getTypeErasure(signature.toCharArray()));
    }

    /**
     * QMyType&lt;QMyClss;QString;&gt;; --&gt; Qmypackage.MyType&lt;Qmylib.MyClss;Qjava.lang.String;&gt;;
     * @param owningType context for the resolution
     * @param signature unresolved signature
     * @return resolved signature
     * @throws SignatureParserException
     * @throws JavaModelException
     */
    public static String resolveSignature(final IType owningType, String signature) throws SignatureParserException, JavaModelException {
        final StringBuilder out = new StringBuilder();

        // create parser that resolves all unresolved types names encountered
        SignatureParser parser = new SignatureParser(signature, new SignatureBuilder(out) {
            boolean isResolved = false;

            @Override
            public void startResolvedType(String identifier) throws ExceptionWrapper {
                isResolved = true;
                super.startResolvedType(identifier);
            }

            @Override
            public void startUnresolvedType(String identifier) throws ExceptionWrapper {
                isResolved = false;
                try {
                    identifier = resolveTypeName(owningType, identifier);
                } catch (JavaModelException e) {
                    throw new ExceptionWrapper(e);
                }
                super.startUnresolvedType(identifier);
            }

            @Override
            public void innerType(String identifier) throws ExceptionWrapper {
                if (!isResolved) {
                    try {
                        identifier = resolveTypeName(owningType, identifier);
                    } catch (JavaModelException e) {
                        throw new ExceptionWrapper(e);
                    }
                }
                super.innerType(identifier);
            }
        });

        // run parser
        try {
            parser.parse();
        } catch (ExceptionWrapper e) {
            e.<JavaModelException>rethrow().done();
        }

        // return resolved signature
        return out.toString();
    }

    /**
     * @param owningType
     * @param signature
     * @return
     * @throws SignatureParserException
     * @throws JavaModelException
     */
    public static IType resolveType(final IType owningType, String signature) throws JavaModelException, SignatureParserException {
        // resolve signature
        String resolvedSignature = getTypeErasure(signature);
        // convert to type name
        String typeName = signatureToTypeName(resolvedSignature);
        // find type
        return findRelativeToHierarchy(owningType, typeName);
    }

    /**
     * @param owningType
     * @param typeSignature
     * @param typeParameterMapping
     * @return
     * @throws SignatureParserException
     * @throws JavaModelException
     */
    public static String resolveTypeWithParameterMapping(IType owningType, String typeSignature,
            Map<String, String> typeParameterMapping) throws JavaModelException, SignatureParserException {
        typeSignature = SignatureResolver.resolveSignature(owningType, typeSignature);
        for (String key : typeParameterMapping.keySet()) {
            String keySignature = Signature.createTypeSignature(key, false);
            typeSignature = typeSignature.replaceAll(keySignature, typeParameterMapping.get(key));
        }
        return typeSignature;
    }

    private static String resolveTypeName(IType owningType, String typeName) throws JavaModelException {
        String[][] resolvedType = owningType.resolveType(typeName);

        if (resolvedType != null && resolvedType.length > 0) {
            return resolvedType[0][0] + "." + resolvedType[0][1];
        }

        return typeName;
    }

    private static IType findRelativeToHierarchy(final IType owningType, String identifier) throws JavaModelException {
        IType t = findRelativeToType(owningType, identifier);
        if (t == null) {
            ITypeHierarchy typeHierarchy;
            typeHierarchy = owningType.newSupertypeHierarchy(null);
            for (IType supertype : typeHierarchy.getAllSupertypes(owningType)) {
                if ((t = findRelativeToType(supertype, identifier)) != null) {
                    break;
                }
            }
        }
        return t;
    }

    private static IType findRelativeToType(final IType owningType, String identifier) throws JavaModelException {
        String[][] resolvedType = owningType.resolveType(identifier);

        if (resolvedType != null && resolvedType.length > 0) {
            return owningType.getJavaProject().findType(resolvedType[0][0], resolvedType[0][1]);
        }

        return null;
    }
}
