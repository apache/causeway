
Imports System
Imports System.Configuration

Imports Spring.Context
Imports Spring.Context.Support
Imports log4net.Config

Imports org.apache.log4j

Imports org.nakedobjects.container.configuration
Imports org.nakedobjects.system

Imports org.nakedobjects.viewer
Imports org.nakedobjects.viewer.skylark
Imports org.nakedobjects.viewer.skylark.special
Imports org.nakedobjects.object
Imports org.nakedobjects.object.fixture
Imports org.nakedobjects.object.reflect
Namespace org.nakedobjects.dotnet


Public MustInherit Class DotNetBootstrapper
        Public MustOverride Sub initExtensions()



        Public Overloads Sub run( _
                         ByVal containerId As String, _
                         ByVal userContextId As String, _
                         ByVal viewerId As String, _
                         ByVal consoleonly As Boolean)


            initSpringContext()

            startLogging()

            'TODO: should both be made properly springable 
            'TODO: watch out for problems with references with the reflector factory

            Dim rf As ReflectorFactory = _
                DirectCast(myCtx.GetObject("SdmReflectorFactory"), ReflectorFactory)

            Dim factory As PojoAdapterFactoryImpl = New PojoAdapterFactoryImpl
            NakedObjects.setAdapterFactory(factory)
            factory.setPojoAdapterHash(New PojoAdapterHashImpl)
            factory.setReflectorFactory(rf)

            initContainer(containerId)
            initDependencies()
            initExtensions()
            If Not consoleOnly Then
                If Not authenticate() Then Return
            End If


        End Sub


        Protected Overridable Sub initDependencies()
        End Sub


        Protected myCtx As IApplicationContext
        Protected Sub initSpringContext()
            myCtx = CType(ConfigurationSettings.GetConfig("spring/context"), IApplicationContext)
        End Sub


        Protected Sub startLogging()
            org.apache.log4j.BasicConfigurator.configure()
            log4net.Config.DOMConfigurator.Configure()
            PropertyConfigurator.configure( _
                NakedObjects.getConfiguration().getProperties("log4j"))
            Dim log As Logger = Logger.getLogger("Naked Objects")
            log.info(AboutNakedObjects.getName())
            log.info(AboutNakedObjects.getVersion())
            log.info(AboutNakedObjects.getBuildId())
        End Sub


        Protected myContainer As FixtureBuilder
        '*
        ' Obtains an implementation of the NOF container (ExplorationSetUp).
        '
        ' Typically the implementation will furnish application-specific 
        ' container functionality, eg dependency injection of services.
        Protected Sub initContainer(ByVal containerId As String)
            myContainer = CType(myCtx.GetObject(containerId), _
                    FixtureBuilder)
        End Sub
        Public ReadOnly Property Container() As FixtureBuilder
            Get
                Return myContainer
            End Get
        End Property

        '*
        ' Hook method that allows subclasses to validate whether the user should
        ' log on.  If return false, then the program quits.
        Protected Overridable Function authenticate() As Boolean
            Return True
        End Function



End Class
End Namespace