Imports System
Imports System.Configuration

Imports Spring.Context
Imports Spring.Context.Support
Imports log4net.Config

Imports org.apache.log4j

Imports org.nakedobjects.container.configuration
Imports org.nakedobjects.system

Imports org.nakedobjects.viewer.skylark
Imports org.nakedobjects.viewer.skylark.special
Imports org.nakedobjects.object
Imports org.nakedobjects.object.fixture
Imports org.nakedobjects.object.reflect

namespace org.nakedobjects.dotnet


Public MustInherit Class DotNetClient
    Public Sub run(ByVal containerId As String, ByVal userContextId As String)
        run(containerId, userContextId, False)
    End Sub
    Public Sub run( _
        ByVal containerId As String, _
        ByVal userContextId As String, _
        ByVal consoleOnly As Boolean)

        Dim splash As SplashWindow
        If Not consoleOnly Then splash = New SplashWindow

        Try
            org.apache.log4j.BasicConfigurator.configure()

            initSpringContext()

            startLogging()

            'TODO: should both be made properly springable 
            'TODO: watch out for problems with references with the reflector factory

            Dim rf As ReflectorFactory = _
                DirectCast(myCtx.GetObject("SdmReflectorFactory"), ReflectorFactory)

                NakedObjects.setPojoAdapterFactory(New PojoAdapterFactory)
                NakedObjects.getPojoAdapterFactory().setPojoAdapterHash(New PojoAdapterHashImpl)
                NakedObjects.getPojoAdapterFactory().setReflectorFactory(rf)

            initContainer(containerId)

            setUpFixtures()
            installFixtures()

            setUpContext(userContextId)

            If Not consoleOnly Then
                If Not authenticate() Then Return
                startViewer()
            End If

        Catch ex As Exception
            ExceptionHelper.dumpException(ex)
            Return
        Finally
            If Not splash Is Nothing Then
                splash.toFront()
                splash.removeAfterDelay(3)
            End If
        End Try

    End Sub

    '*
    ' Hook method that allows subclasses to validate whether the user should
    ' log on.  If return false, then the program quits.
    Protected Overridable Function authenticate() As Boolean
        Return True
    End Function


    Private myCtx As IApplicationContext
    Private Sub initSpringContext()
        myCtx = CType(ConfigurationSettings.GetConfig("spring/context"), IApplicationContext)
    End Sub


    Private Sub startLogging()
        PropertyConfigurator.configure( _
            ConfigurationFactory.getConfiguration().getProperties("log4j"))
        Dim log As Logger = Logger.getLogger("Naked Objects")
        log.info(AboutNakedObjects.getName())
        log.info(AboutNakedObjects.getVersion())
        log.info(AboutNakedObjects.getBuildId())
    End Sub


    Private myContainer As FixtureBuilder
    '*
    ' Obtains an implementation of the NOF container (ExplorationSetUp).
    '
    ' Typically the implementation will furnish application-specific 
    ' container functionality, eg dependency injection of services.
    Private Sub initContainer(ByVal containerId As String)
        myContainer = CType(myCtx.GetObject(containerId), _
                FixtureBuilder)
    End Sub
    Public ReadOnly Property Container() As FixtureBuilder
        Get
            Return myContainer
        End Get
    End Property


    '*
    ' Sets up a UserContext (to create root view) using supplied string 
    ' (eg "UserContext").
    '
    Private Sub setUpContext(ByVal userContextId As String)

        Dim mviewer As org.nakedobjects.viewer.skylark.Viewer = _
            CType(myCtx.GetObject("Viewer"), _
            org.nakedobjects.viewer.skylark.Viewer)
        mviewer.start()

        Dim applicationContext As DotNetUserContext = _
            CType(myCtx.GetObject(userContextId), DotNetUserContext)
        applicationContext.created()

            Dim root As NakedObject = _
                NakedObjects.getPojoAdapterFactory().createNOAdapter(applicationContext)
        Dim spec As New RootWorkspaceSpecification

        Dim v As View = spec.createView(New RootObject(root), Nothing)
        mviewer.setRootView(v)

    End Sub


    '*
    ' Also, obtains a Viewer using "Viewer", ViewerFrame using "ViewerFrame",
    '
    Private Sub startViewer()

        Dim mviewer As org.nakedobjects.viewer.skylark.Viewer = _
            CType(myCtx.GetObject("Viewer"), _
            org.nakedobjects.viewer.skylark.Viewer)

        Dim frame As ViewerFrame = _
            CType(myCtx.GetObject("ViewerFrame"), ViewerFrame)

        frame.setBounds(10, 10, 800, 600)

        mviewer.sizeChange()
        
            mviewer.setListener(New DotNetClientShutdown)

        frame.show()

    End Sub

    '*
    ' Adds fixture to the container
    Protected Sub addFixture(ByVal fixture As org.nakedobjects.object.fixture.Fixture)
        myContainer.addFixture(fixture)
    End Sub

    '*
    ' Installs fixture into the container
    Private Sub installFixtures()
        myContainer.installFixtures()
    End Sub


    Protected MustOverride Sub setUpFixtures()

End Class

end namespace
