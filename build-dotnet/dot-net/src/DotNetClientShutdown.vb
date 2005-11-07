Imports System

Imports org.nakedobjects.event

namespace org.nakedobjects.dotnet


    Public Class DotNetClientShutdown
        Implements ObjectViewingMechanismListener

        Public Sub viewerClosing() Implements ObjectViewingMechanismListener.viewerClosing
            Environment.Exit(0)
        End Sub
    End Class

end namespace