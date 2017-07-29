package org.grails.gorm.graphql.entity.dsl.helpers

import graphql.schema.GraphQLInputObjectType
import graphql.schema.GraphQLInputType
import graphql.schema.GraphQLList
import graphql.schema.GraphQLNonNull
import graphql.schema.GraphQLOutputType
import groovy.transform.CompileStatic
import graphql.schema.GraphQLObjectType
import org.grails.datastore.mapping.model.MappingContext
import org.grails.gorm.graphql.entity.dsl.ComplexField
import org.grails.gorm.graphql.entity.dsl.Field
import org.grails.gorm.graphql.entity.dsl.SimpleField
import org.grails.gorm.graphql.types.GraphQLTypeManager

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition
import static graphql.schema.GraphQLInputObjectField.newInputObjectField

@CompileStatic
trait ComplexTyped<T> {

    boolean collection = false

    T collection(boolean collection) {
        this.collection = collection
        (T)this
    }

    List<Field> fields = []

    /**
     * This method exists because of https://issues.apache.org/jira/browse/GROOVY-8272
     *
     * Normally the {@link ExecutesClosures} trait would be extended from
     */
    private void withDelegate(Closure closure, Object delegate) {
        if (closure != null) {
            closure.resolveStrategy = Closure.DELEGATE_ONLY
            closure.delegate = delegate

            try {
                closure.call()
            } finally {
                closure.delegate = null
            }
        }
    }

    /**
     * Builds a custom object returnType if the supplied return returnType is a Map
     *
     * @param typeManager The returnType manager
     * @param mappingContext The mapping context
     * @return The custom returnType
     */
    GraphQLOutputType buildCustomType(String name, GraphQLTypeManager typeManager, MappingContext mappingContext) {
        GraphQLObjectType.Builder builder = GraphQLObjectType.newObject()
                .name(name)

        for (Field field: fields) {
            if (field.output) {
                builder.field(newFieldDefinition()
                        .name(field.name)
                        .description(field.description)
                        .deprecate(field.deprecationReason)
                        .type(field.getType(typeManager, mappingContext)))
            }
        }
        GraphQLObjectType type = builder.build()

        if (collection) {
            GraphQLList.list(type)
        }
        else {
            type
        }
    }

    /**
     * Builds a custom object returnType if the supplied return returnType is a Map
     *
     * @param typeManager The returnType manager
     * @param mappingContext The mapping context
     * @return The custom returnType
     */
    GraphQLInputType buildCustomInputType(String name, GraphQLTypeManager typeManager, MappingContext mappingContext, boolean nullable) {
        GraphQLInputObjectType.Builder builder = GraphQLInputObjectType.newInputObject()
                .name(name)

        for (Field field: fields) {
            if (field.input) {
                builder.field(newInputObjectField()
                        .name(field.name)
                        .description(field.description)
                        .defaultValue(field.defaultValue)
                        .type(field.getInputType(typeManager, mappingContext)))
            }
        }
        GraphQLInputType type = builder.build()

        if (!nullable) {
            type = GraphQLNonNull.nonNull(type)
        }

        if (collection) {
            GraphQLList.list(type)
        }
        else {
            type
        }
    }

    private void handleField(Closure closure, Field field) {
        withDelegate(closure, field)
        field.validate()
        fields.add(field)
    }

    void field(String name, List<Class> type, @DelegatesTo(value = SimpleField, strategy = Closure.DELEGATE_ONLY) Closure closure = null) {
        Field field = new SimpleField().name(name).returns(type)
        handleField(closure, field)
    }

    void field(String name, Class type, @DelegatesTo(value = SimpleField, strategy = Closure.DELEGATE_ONLY) Closure closure = null) {
        Field field = new SimpleField().name(name).returns(type)
        handleField(closure, field)
    }

    void field(String name, String typeName, @DelegatesTo(value = ComplexField, strategy = Closure.DELEGATE_ONLY) Closure closure) {
        Field field = new ComplexField().name(name).typeName(typeName)
        handleField(closure, field)
    }

}