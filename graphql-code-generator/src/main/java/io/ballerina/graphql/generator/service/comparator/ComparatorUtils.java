package io.ballerina.graphql.generator.service.comparator;

import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.DistinctTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.EnumMemberNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.IntersectionTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.MethodDeclarationNode;
import io.ballerina.compiler.syntax.tree.MinutiaeList;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ObjectTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.StreamTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.StreamTypeParamsNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.graphql.generator.CodeGeneratorConstants;
import io.ballerina.graphql.generator.service.Constants;

import java.util.ArrayList;
import java.util.List;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createCommentMinutiae;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyMinutiaeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEndOfLineMinutiae;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createMinutiaeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;

/**
 * Utility class with helper functions needed for EqualityResult classes.
 */
public class ComparatorUtils {
    public static String getEnumMemberName(Node enumMember) {
        if (enumMember instanceof EnumMemberNode) {
            EnumMemberNode enumMemberNode = (EnumMemberNode) enumMember;
            return enumMemberNode.identifier().text();
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
            String mainQualifier = getMainQualifier(methodDeclaration.qualifierList()).text();
            if (mainQualifier.equals(Constants.RESOURCE)) {
                Node methodDeclarationNameNode = methodDeclaration.relativeResourcePath().get(0);
                if (methodDeclarationNameNode instanceof IdentifierToken) {
                    IdentifierToken methodDeclarationName = (IdentifierToken) methodDeclarationNameNode;
                    return methodDeclarationName.text();
                }
            } else if (mainQualifier.equals(Constants.REMOTE)) {
                return methodDeclaration.methodName().text();
            } else {
                return methodDeclaration.methodName().text();
            }
        } else {
            return methodDeclaration.methodName().text();
        }
        return null;
    }

    public static String getMethodDeclarationResolverType(MethodDeclarationNode methodDeclaration) {
        if (methodDeclaration.methodName().text().equals(CodeGeneratorConstants.GET)) {
            return CodeGeneratorConstants.GET;
        } else if (methodDeclaration.methodName().text().equals(CodeGeneratorConstants.SUBSCRIBE)) {
            return CodeGeneratorConstants.SUBSCRIBE;
        } else {
            return null;
        }
    }

    public static String getFunctionDefinitionResolverType(FunctionDefinitionNode functionDefinition) {
        if (functionDefinition.functionName().text().equals(CodeGeneratorConstants.GET)) {
            return CodeGeneratorConstants.GET;
        } else if (functionDefinition.functionName().text().equals(CodeGeneratorConstants.SUBSCRIBE)) {
            return CodeGeneratorConstants.SUBSCRIBE;
        } else {
            return null;
        }
    }

    public static String getFunctionName(FunctionDefinitionNode functionDefinition) {
        if (functionDefinition.qualifierList().size() > 0) {
            String mainQualifier = getMainQualifier(functionDefinition.qualifierList()).text();
            if (mainQualifier.equals(Constants.RESOURCE)) {
                Node functionNameNode = functionDefinition.relativeResourcePath().get(0);
                if (functionNameNode instanceof IdentifierToken) {
                    IdentifierToken functionNameToken = (IdentifierToken) functionNameNode;
                    return functionNameToken.text();
                }
            } else if (mainQualifier.equals(Constants.REMOTE)) {
                return functionDefinition.functionName().text();
            }
        } else {
            return functionDefinition.functionName().text();
        }

        return null;
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

    public static ParameterComparator isParameterEquals(ParameterNode prevParameter, ParameterNode nextParameter) {
        ParameterComparator parameterEquality = new ParameterComparator(prevParameter, nextParameter);
        TypeComparator typeEquals =
                new TypeComparator(getParameterType(prevParameter), getParameterType(nextParameter));
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

    public static NodeList<Token> getMergedFunctionDefinitionQualifiers(NodeList<Token> prevQualifiers,
                                                                        NodeList<Token> nextQualifiers,
                                                                        boolean isFirstFunctionDefinition) {
        return getMergedQualifiers(prevQualifiers, nextQualifiers, true, isFirstFunctionDefinition);
    }

    public static NodeList<Token> getMergedMethodDeclarationQualifiers(NodeList<Token> prevQualifiers,
                                                                       NodeList<Token> nextQualifiers) {
        return getMergedQualifiers(prevQualifiers, nextQualifiers, false, false);
    }

    public static NodeList<Token> getMergedQualifiers(
            NodeList<Token> prevQualifiers, NodeList<Token> nextQualifiers,
            boolean addNewLineInFront, boolean isFirst) {
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
                    Token modifiedNextMainQualifier =
                            generateQualifierToken(nextMainQualifier.text(),
                                    addNewLineInFront && !isFirst);
                    mergedQualifiers.add(modifiedNextMainQualifier);
                }
                addNewLineInFront = false;
            }
        }
        return createNodeList(mergedQualifiers);
    }

    public static boolean isMetadataEqual(MetadataNode prevMetadata, MetadataNode nextMetadata) {
        if (prevMetadata == null && nextMetadata == null) {
            return true;
        } else if (prevMetadata != null && nextMetadata != null) {
            return prevMetadata.toString().equals(nextMetadata.toString());
        } else {
            return false;
        }
    }

    public static Token generateQualifierToken(String qualifier, boolean addNewLineInFront) {
        MinutiaeList leadingMinutiaeList = createEmptyMinutiaeList();
        if (addNewLineInFront) {
            leadingMinutiaeList =
                    leadingMinutiaeList.add(createCommentMinutiae(CodeGeneratorConstants.NEW_LINE));
        }
        SyntaxKind keyword = null;
        if (qualifier.equals(Constants.REMOTE)) {
            keyword = SyntaxKind.REMOTE_KEYWORD;
        } else if (qualifier.equals(Constants.RESOURCE)) {
            keyword = SyntaxKind.RESOURCE_KEYWORD;
        }
        return createToken(keyword, leadingMinutiaeList, createEmptyMinutiaeList());
    }

    public static ObjectTypeDescriptorNode generateObjectType(Node typeDescriptor) {
        if (typeDescriptor instanceof IntersectionTypeDescriptorNode) {
            IntersectionTypeDescriptorNode intersectionTypeDescriptor = (IntersectionTypeDescriptorNode) typeDescriptor;
            return generateObjectType(intersectionTypeDescriptor.rightTypeDesc());
        } else if (typeDescriptor instanceof ObjectTypeDescriptorNode) {
            return (ObjectTypeDescriptorNode) typeDescriptor;
        } else if (typeDescriptor instanceof DistinctTypeDescriptorNode) {
            DistinctTypeDescriptorNode distinctTypeDescriptor = (DistinctTypeDescriptorNode) typeDescriptor;
            return generateObjectType(distinctTypeDescriptor.typeDescriptor());
        } else {
            return null;
        }
    }
}
