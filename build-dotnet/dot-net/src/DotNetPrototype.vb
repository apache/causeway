
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


Public MustInherit Class DotNetPrototype
    Inherits DotNetBootstrapper

        Public Overridable Overloads Sub run(ByVal containerId As String, ByVal userContextId As String, ByVal viewerId As String)
            Me.run(containerId, userContextId, viewerId, False)
        End Sub

        Public Overridable Overloads Sub run(ByVal containerId As String, _
ByVal userContextId As String, _
ByVal viewerId As String, _
ByVal consoleOnly As Boolean)
            'To-Do needs to be an interface
            Dim thisViewer As org.nakedobjects.viewer.skylark.SkylarkViewer

            Dim splash As SplashWindow
            If Not consoleOnly Then splash = New SplashWindow

            Try

                MyBase.run(containerId, userContextId, viewerId, consoleOnly)
                
                thisViewer = _
                       New org.nakedobjects.viewer.skylark.SkylarkViewer
                Dim applicationContext As DotNetUserContext = _
                    CType(myCtx.GetObject(userContextId), DotNetUserContext)
                applicationContext.created()

                'Retrieve the clientShutdownlistener from spring
                Dim shutdownlistener As ObjectViewingMechanismListener
                shutdownlistener = _
                    CType(myCtx.GetObject("ShutdownListener"), ObjectViewingMechanismListener)
                Dim updateNotifier As ViewUpdateNotifier
                updateNotifier = _
                    CType(myCtx.GetObject("UpdateNotifier"), ViewUpdateNotifier)

                thisViewer.setUpdateNotifier(updateNotifier)
                thisViewer.setApplication(applicationContext)
                thisViewer.setShutdownListener(shutdownlistener)
                thisViewer.setExploration(True)
                thisViewer.init()
            Catch ex As Exception
                ExceptionHelper.dumpException(ex)
                Throw ex
            Finally
                thisViewer = Nothing
                If Not splash Is Nothing Then
                    splash.toFront()
                    splash.removeAfterDelay(3)
                End If
            End Try


        End Sub

        Public Overrides Sub initExtensions()
            setUpFixtures()
            installFixtures()
            fixturesInstalled()
        End Sub



        '*
        ' Do-nothing hook method to inform subclasses that all fixtures have been installed.
        Protected Overridable Sub fixturesInstalled()

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
End Namespace