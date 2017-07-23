package gorm.graphql

import grails.plugins.Plugin
import grails.web.mime.MimeType
import graphql.GraphQL
import org.grails.gorm.graphql.Schema
import org.grails.gorm.graphql.binding.manager.DefaultGraphQLDataBinderManager
import org.grails.gorm.graphql.entity.GraphQLEntityNamingConvention
import org.grails.gorm.graphql.entity.property.manager.DefaultGraphQLDomainPropertyManager
import org.grails.gorm.graphql.fetcher.manager.DefaultGraphQLDataFetcherManager
import org.grails.gorm.graphql.response.delete.DefaultGraphQLDeleteResponseHandler
import org.grails.gorm.graphql.response.errors.DefaultGraphQLErrorsResponseHandler
import org.grails.gorm.graphql.types.DefaultGraphQLTypeManager

class GormGraphqlGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "3.2.9 > *"


    // TODO Fill in these fields
    def title = "Gorm Graphql" // Headline display name of the plugin
    def author = "James Kleeh"
    def authorEmail = "james.kleeh@gmail.com"
    def description = '''\
Brief summary/description of the plugin.
'''
    def profiles = ['web']

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/gorm-graphql"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
//    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
//    def organization = [ name: "My Company", url: "http://www.my-company.com/" ]

    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    // Location of the plugin's issue tracker.
//    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPMYPLUGIN" ]

    // Online location of the plugin's browseable source code.
//    def scm = [ url: "http://svn.codehaus.org/grails-plugins/" ]

    public static MimeType GRAPHQL_MIME =  new MimeType('application/graphql')

    Closure doWithSpring() {{ ->

        if (!config.getProperty('grails.gorm.graphql.enabled', Boolean, true)) {
            return
        }

        graphQLDataBinder(GrailsGraphQLDataBinder)
        graphQLErrorsResponseHandler(DefaultGraphQLErrorsResponseHandler, ref("messageSource"))
        graphQLDeleteResponseHandler(DefaultGraphQLDeleteResponseHandler)
        graphQLEntityNamingConvention(GraphQLEntityNamingConvention)
        graphQLDomainPropertyManager(DefaultGraphQLDomainPropertyManager)
        graphQLTypeManager(DefaultGraphQLTypeManager, ref("graphQLEntityNamingConvention"), ref("graphQLErrorsResponseHandler"), ref("graphQLDomainPropertyManager"))
        graphQLDataBinderManager(DefaultGraphQLDataBinderManager, ref("graphQLDataBinder"))
        graphQLDataFetcherManager(DefaultGraphQLDataFetcherManager)

        grailsGraphQLConfiguration(GrailsGraphQLConfiguration)

        graphQLSchemaGenerator(Schema, ref("grailsDomainClassMappingContext")) {
            deleteResponseHandler = ref("graphQLDeleteResponseHandler")
            namingConvention = ref("graphQLEntityNamingConvention")
            typeManager = ref("graphQLTypeManager")
            dataBinderManager = ref("graphQLDataBinderManager")
            dataFetcherManager = ref("graphQLDataFetcherManager")
            dateFormats = '#{grailsGraphQLConfiguration.getDateFormats()}'
            dateFormatLenient = '#{grailsGraphQLConfiguration.getDateFormatLenient()}'
            listArguments = '#{grailsGraphQLConfiguration.getListArguments()}'
        }

        graphQLSchema(graphQLSchemaGenerator: "generate")
        graphQL(GraphQL, ref("graphQLSchema"))
    }}
}