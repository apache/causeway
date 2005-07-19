Imports System
Imports System.Configuration

Imports Spring.Context
Imports Spring.Context.Support

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


    Public MustInherit Class DotNetClient
        Inherits DotNetBootstrapper

        '     Public Overridable Overloads Sub run(ByVal containerId As String, _
        '    ByVal userContextId As String, _
        '   ByVal viewerId As String)
        '      Me.run(containerId, userContextId, viewerId, False)
        ' End Sub


        Public Overrides Sub display(ByVal userContextId As String)

            'To-Do needs to be an interface
            Dim thisViewer As org.nakedobjects.viewer.skylark.SkylarkViewer

            '         Dim splash As SplashWindow
            '       If Not consoleOnly Then splash = New SplashWindow

            '  Try

            '  MyBase.run(containerId, userContextId, viewerId, consoleOnly)

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
            'Need to set the Exploration off 
            thisViewer.setExploration(False)
            thisViewer.init()


            'Catch ex As Exception
            '    ExceptionHelper.dumpException(ex)
            '    Throw ex
            ' Finally
            '    thisViewer = Nothing
            'If Not splash Is Nothing Then
            '    splash.toFront()
            '    splash.removeAfterDelay(3)
            'End If
            '  End Try


        End Sub

        Public Overrides Sub initExtensions()
        End Sub




    End Class

End Namespace
