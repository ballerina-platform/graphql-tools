package io.ballerina.graphql.generator.service;

import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.EnumDeclarationNode;
import io.ballerina.compiler.syntax.tree.EnumMemberNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.MethodDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.StreamTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.StreamTypeParamsNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.graphql.generator.CodeGeneratorConstants;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyMinutiaeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEndOfLineMinutiae;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createMinutiaeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;

/**
 * Utility class with helper functions needed for EqualityResult classes.
 */
public class EqualityResultUtils {
    public static String getEnumMemberName(Node enumMember) {
        if (enumMember instanceof EnumMemberNode) {
            EnumMemberNode enumMemberNode = (EnumMemberNode) enumMember;
            return enumMemberNode.identifier().text();
        }
        return null;
    }

    public static MetadataNode getEnumMemberMetadata(Node member) {
        if (member instanceof EnumMemberNode) {
            EnumMemberNode enumMember = (EnumMemberNode) member;
            return enumMember.metadata().orElse(null);
        }
        return null;
    }

    public static MetadataNode getEnumMetadata(Node member) {
        if (member instanceof EnumDeclarationNode) {
            EnumDeclarationNode enumDeclaration = (EnumDeclarationNode) member;
            return enumDeclaration.metadata().orElse(null);
        }
        return null;
    }

    public static SeparatedNodeList<Node> getCommaAddedSeparatedNodeList(List<Node> nodes) {
        List<Node> commaAddedNodes = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {
            commaAddedNodes.add(nodes.get(i));
            if (i != nodes.size() - 1) {
                commaAddedNodes.add(createToken(SyntaxKind.COMMA_TOKEN, createEmptyMinutiaeList(),
                        createMinutiaeList(createEndOfLineMinutiae(CodeGeneratorConstants.NEW_LINE))));
            }
        }
        return createSeparatedNodeList(commaAddedNodes);
    }

    public static String getRecordFieldName(Node field) {
        if (field instanceof RecordFieldNode) {
            RecordFieldNode recordField = (RecordFieldNode) field;
            return recordField.fieldName().text();
        } else if (field instanceof RecordFieldWithDefaultValueNode) {
            RecordFieldWithDefaultValueNode recordFieldWithDefaultValue = (RecordFieldWithDefaultValueNode) field;
            return recordFieldWithDefaultValue.fieldName().text();
        }
        return null;
    }

    public static Node getRecordFieldType(Node field) {
        if (field instanceof RecordFieldNode) {
            RecordFieldNode recordField = (RecordFieldNode) field;
            return recordField.typeName();
        } else if (field instanceof RecordFieldWithDefaultValueNode) {
            RecordFieldWithDefaultValueNode recordFieldWithDefaultValue = (RecordFieldWithDefaultValueNode) field;
            return recordFieldWithDefaultValue.typeName();
        }
        return null;
    }

    public static MethodDeclarationEqualityResult isMethodDeclarationEquals(
            MethodDeclarationNode prevMethodDeclaration, MethodDeclarationNode nextMethodDeclaration) {
        MethodDeclarationEqualityResult methodDeclarationEquality =
                new MethodDeclarationEqualityResult(prevMethodDeclaration, nextMethodDeclaration);
        methodDeclarationEquality.setPrevFunctionName(getMethodDeclarationName(prevMethodDeclaration));
        methodDeclarationEquality.setNextFunctionName(getMethodDeclarationName(nextMethodDeclaration));
        methodDeclarationEquality.setPrevQualifiers(prevMethodDeclaration.qualifierList());
        methodDeclarationEquality.setNextQualifiers(nextMethodDeclaration.qualifierList());
        methodDeclarationEquality.setPrevMethodType(prevMethodDeclaration.methodName().text());
        methodDeclarationEquality.setNextMethodType(nextMethodDeclaration.methodName().text());
        if (isRelativeResourcePathEquals(prevMethodDeclaration.relativeResourcePath(),
                nextMethodDeclaration.relativeResourcePath())) {
            methodDeclarationEquality.setRelativeResourcePathsEqual(true);
        }
        FunctionSignatureEqualityResult funcSignatureEquals =
                isFuncSignatureEquals(prevMethodDeclaration.methodSignature(), nextMethodDeclaration.methodSignature());
        methodDeclarationEquality.setFunctionSignatureEqualityResult(funcSignatureEquals);
        return methodDeclarationEquality;
    }

    public static boolean isRelativeResourcePathEquals(NodeList<Node> prevRelativeResourcePath,
                                                       NodeList<Node> nextRelativeResourcePath) {
        if (!(prevRelativeResourcePath.size() == nextRelativeResourcePath.size())) {
            return false;
        }
        for (int i = 0; i < prevRelativeResourcePath.size(); i++) {
            Node prevRelativeResource = prevRelativeResourcePath.get(i);
            Node nextRelativeResource = nextRelativeResourcePath.get(i);
            if (prevRelativeResource instanceof IdentifierToken && nextRelativeResource instanceof IdentifierToken) {
                IdentifierToken prevIdentifier = (IdentifierToken) prevRelativeResource;
                IdentifierToken nextIdentifier = (IdentifierToken) nextRelativeResource;
                if (!prevIdentifier.text().equals(nextIdentifier.text())) {
                    return false;
                }
            }
        }
        return true;
    }

    public static String getMethodDeclarationName(MethodDeclarationNode methodDeclaration) {
        if (methodDeclaration.qualifierList().size() > 0) {
            String firstQualifier = methodDeclaration.qualifierList().get(0).text();
            if (firstQualifier.equals(Constants.RESOURCE)) {
                Node methodDeclarationNameNode = methodDeclaration.relativeResourcePath().get(0);
                if (methodDeclarationNameNode instanceof IdentifierToken) {
                    IdentifierToken methodDeclarationName = (IdentifierToken) methodDeclarationNameNode;
                    return methodDeclarationName.text();
                }
            } else if (firstQualifier.equals(Constants.REMOTE)) {
                return methodDeclaration.methodName().text();
            }
        } else {
            return methodDeclaration.methodName().text();
        }
        return null;
    }

    public static String getFunctionName(FunctionDefinitionNode functionDefinition) {
        if (functionDefinition.qualifierList().size() > 0) {
            String firstQualifier = functionDefinition.qualifierList().get(0).text();
            if (firstQualifier.equals(Constants.RESOURCE)) {
                Node functionNameNode = functionDefinition.relativeResourcePath().get(0);
                if (functionNameNode instanceof IdentifierToken) {
                    IdentifierToken functionNameToken = (IdentifierToken) functionNameNode;
                    return functionNameToken.text();
                }
            } else if (firstQualifier.equals(Constants.REMOTE)) {
                return functionDefinition.functionName().text();
            }
        } else {
            return functionDefinition.functionName().text();
        }

        return null;
    }

    public static FunctionDefinitionEqualityResult isFuncDefEquals(FunctionDefinitionNode prevClassFuncDef,
                                                                   FunctionDefinitionNode nextClassFuncDef) {
        FunctionDefinitionEqualityResult functionDefinitionEquality =
                new FunctionDefinitionEqualityResult(prevClassFuncDef, nextClassFuncDef);
        functionDefinitionEquality.setPrevFunctionName(getFunctionName(prevClassFuncDef));
        functionDefinitionEquality.setNextFunctionName(getFunctionName(nextClassFuncDef));
        functionDefinitionEquality.setPrevQualifiers(prevClassFuncDef.qualifierList());
        functionDefinitionEquality.setNextQualifiers(nextClassFuncDef.qualifierList());
        functionDefinitionEquality.setPrevMethodType(prevClassFuncDef.functionName().text());
        functionDefinitionEquality.setNextMethodType(nextClassFuncDef.functionName().text());
        functionDefinitionEquality.setFunctionNameEqual(
                prevClassFuncDef.functionName().text().equals(nextClassFuncDef.functionName().text()));
        functionDefinitionEquality.setRelativeResourcePathsEqual(
                isRelativeResourcePathEquals(prevClassFuncDef.relativeResourcePath(),
                        nextClassFuncDef.relativeResourcePath()));
        FunctionSignatureEqualityResult funcSignatureEquals =
                isFuncSignatureEquals(prevClassFuncDef.functionSignature(), nextClassFuncDef.functionSignature());
        functionDefinitionEquality.setFunctionSignatureEqualityResult(funcSignatureEquals);
        return functionDefinitionEquality;
    }

    public static FunctionSignatureEqualityResult isFuncSignatureEquals(FunctionSignatureNode prevFunctionSignature,
                                                                        FunctionSignatureNode nextFunctionSignature) {
        FunctionSignatureEqualityResult equalityResult = new FunctionSignatureEqualityResult();
        LinkedHashMap<ParameterNode, Boolean> nextParameterAvailable = new LinkedHashMap<>();
        for (ParameterNode nextParameter : nextFunctionSignature.parameters()) {
            nextParameterAvailable.put(nextParameter, false);
        }
        for (ParameterNode prevParameter : prevFunctionSignature.parameters()) {
            boolean foundMatch = false;
            for (ParameterNode nextParameter : nextFunctionSignature.parameters()) {
                ParameterEqualityResult parameterEquals = isParameterEquals(prevParameter, nextParameter);
                if (parameterEquals.isEqual()) {
                    foundMatch = true;
                    nextParameterAvailable.put(nextParameter, true);
                    break;
                } else {
                    if (parameterEquals.isMatch()) {
                        foundMatch = true;
                        nextParameterAvailable.put(nextParameter, true);
                        if (!parameterEquals.getTypeEquality().isEqual()) {
                            equalityResult.addToTypeChangedParameters(parameterEquals);
                        }
                        if (parameterEquals.isDefaultValueRemoved()) {
                            equalityResult.addToDefaultValueRemovedParameters(parameterEquals);
                        }
                    }
                }
            }
            if (!foundMatch) {
                equalityResult.addToRemovedParameters(getParameterName(prevParameter));
            }
        }
        for (Map.Entry<ParameterNode, Boolean> entry : nextParameterAvailable.entrySet()) {
            Boolean parameterAvailable = entry.getValue();
            if (!parameterAvailable) {
                ParameterNode newParameter = entry.getKey();
                String newParameterName = getParameterName(newParameter);
                if (newParameter instanceof RequiredParameterNode) {
                    equalityResult.addToAddedViolatedParameters(newParameterName);
                }
                equalityResult.addToAddedParameters(newParameterName);
            }
        }
        ReturnTypeDescriptorNode prevReturnType = prevFunctionSignature.returnTypeDesc().orElseThrow();
        ReturnTypeDescriptorNode nextReturnType = nextFunctionSignature.returnTypeDesc().orElseThrow();

        TypeEqualityResult returnTypeEqualityResult = isTypeEquals(prevReturnType.type(), nextReturnType.type());
        equalityResult.setTypeEqualityResult(returnTypeEqualityResult);
        return equalityResult;
    }

    public static String getParameterName(ParameterNode parameter) {
        if (parameter instanceof RequiredParameterNode) {
            RequiredParameterNode requiredParameter = (RequiredParameterNode) parameter;
            return requiredParameter.paramName().orElse(null).text();
        } else if (parameter instanceof DefaultableParameterNode) {
            DefaultableParameterNode defaultableParameter = (DefaultableParameterNode) parameter;
            return defaultableParameter.paramName().orElse(null).text();
        }
        return null;
    }

    public static String getParameterDefaultValue(ParameterNode parameter) {
        if (parameter instanceof DefaultableParameterNode) {
            DefaultableParameterNode defaultableParameter = (DefaultableParameterNode) parameter;
            Node defaultExpression = defaultableParameter.expression();
            return defaultExpression.toString();
        }
        return null;
    }

    public static TypeEqualityResult isTypeEquals(Node prevType, Node nextType) {
        TypeEqualityResult equalityResult = new TypeEqualityResult();
        equalityResult.setPrevType(getTypeName(prevType));
        equalityResult.setNextType(getTypeName(nextType));
        return equalityResult;
    }

    public static String getTypeName(Node type) {
        if (type instanceof BuiltinSimpleNameReferenceNode) {
            BuiltinSimpleNameReferenceNode typeName = (BuiltinSimpleNameReferenceNode) type;
            return typeName.name().text();
        } else if (type instanceof SimpleNameReferenceNode) {
            SimpleNameReferenceNode typeName = (SimpleNameReferenceNode) type;
            return typeName.name().text();
        } else if (type instanceof QualifiedNameReferenceNode) {
            QualifiedNameReferenceNode typeName = (QualifiedNameReferenceNode) type;
            return typeName.identifier().text();
        } else if (type instanceof OptionalTypeDescriptorNode) {
            OptionalTypeDescriptorNode wrappedType = (OptionalTypeDescriptorNode) type;
            return String.format("%s?", getTypeName(wrappedType.typeDescriptor()));
        } else if (type instanceof ArrayTypeDescriptorNode) {
            ArrayTypeDescriptorNode wrappedType = (ArrayTypeDescriptorNode) type;
            return String.format("%s[]", getTypeName(wrappedType.memberTypeDesc()));
        } else if (type instanceof StreamTypeDescriptorNode) {
            StreamTypeDescriptorNode wrappedType = (StreamTypeDescriptorNode) type;
            StreamTypeParamsNode streamParams = (StreamTypeParamsNode) wrappedType.streamTypeParamsNode().orElseThrow();
            String typesCommaSeparatedNames = getTypeName(streamParams.leftTypeDescNode());
            if (streamParams.rightTypeDescNode().isPresent()) {
                typesCommaSeparatedNames = typesCommaSeparatedNames.concat(",");
                typesCommaSeparatedNames =
                        typesCommaSeparatedNames.concat(getTypeName(streamParams.rightTypeDescNode().orElseThrow()));
            }
            return String.format("stream<%s>", typesCommaSeparatedNames);
        } else {
            return null;
        }
    }

    public static ParameterEqualityResult isParameterEquals(ParameterNode prevParameter, ParameterNode nextParameter) {
        ParameterEqualityResult parameterEquality = new ParameterEqualityResult(prevParameter, nextParameter);
        parameterEquality.setPrevParameterName(getParameterName(prevParameter));
        parameterEquality.setNextParameterName(getParameterName(nextParameter));
        parameterEquality.setPrevParameterDefaultValue(getParameterDefaultValue(prevParameter));
        parameterEquality.setNextParameterDefaultValue(getParameterDefaultValue(nextParameter));
        TypeEqualityResult typeEquals = isTypeEquals(getParameterType(prevParameter), getParameterType(nextParameter));
        parameterEquality.setTypeEquality(typeEquals);
        return parameterEquality;
    }

    public static Node getParameterType(ParameterNode parameter) {
        if (parameter instanceof RequiredParameterNode) {
            RequiredParameterNode requiredParameter = (RequiredParameterNode) parameter;
            return requiredParameter.typeName();
        } else if (parameter instanceof DefaultableParameterNode) {
            DefaultableParameterNode defaultableParameter = (DefaultableParameterNode) parameter;
            return defaultableParameter.typeName();
        } else {
            return null;
        }
    }

    public static Token getMainQualifier(NodeList<Token> qualifiers) {
        for (Token qualifier : qualifiers) {
            if (qualifier.text().equals(Constants.RESOURCE) ||
                    qualifier.text().equals(Constants.REMOTE)) {
                return qualifier;
            }
        }
        return null;
    }

    public static boolean isResolverFunction(FunctionDefinitionNode functionDefinition) {
        Token mainQualifier = getMainQualifier(functionDefinition.qualifierList());
        if (mainQualifier != null &&
                (mainQualifier.text().equals(Constants.RESOURCE) || mainQualifier.text().equals(Constants.REMOTE))) {
            return true;
        }
        return false;
    }

    public static boolean isResolverMethod(MethodDeclarationNode prevMethodDeclaration) {
        Token mainQualifier = getMainQualifier(prevMethodDeclaration.qualifierList());
        if (mainQualifier != null &&
                (mainQualifier.text().equals(Constants.RESOURCE) || mainQualifier.text().equals(Constants.REMOTE))) {
            return true;
        }
        return false;
    }

    public static NodeList<Token> getMergedQualifiers(NodeList<Token> prevQualifiers, NodeList<Token> nextQualifiers) {
        List<Token> mergedQualifiers = new ArrayList<>();
        Token prevMainQualifier = getMainQualifier(prevQualifiers);
        Token nextMainQualifier = getMainQualifier(nextQualifiers);
        if (prevMainQualifier == null) {
            return nextQualifiers;
        } else {
            for (Token prevQualifier : prevQualifiers) {
                if (!prevQualifier.equals(prevMainQualifier)) {
                    mergedQualifiers.add(prevQualifier);
                } else {
                    mergedQualifiers.add(nextMainQualifier);
                }
            }
        }
        return createNodeList(mergedQualifiers);
    }

    public static MetadataNode getMergedMetadata(MetadataNode prevMetadata, MetadataNode nextMetadata) {
        if (nextMetadata == null) {
            return prevMetadata;
        }
        return nextMetadata;
    }
}
