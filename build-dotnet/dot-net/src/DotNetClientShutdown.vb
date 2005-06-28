Imports System

Imports org.nakedobjects.viewer

namespace org.nakedobjects.dotnet


    Public Class DotNetClientShutdown
        Implements ObjectViewingMechanismListener

        Public Sub viewerClosing() Implements org.nakedobjects.viewer.ObjectViewingMechanismListener.viewerClosing
            Environment.Exit(0)
        End Sub
    End Class

end namespace