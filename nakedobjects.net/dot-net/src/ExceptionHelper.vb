Imports System


namespace org.nakedobjects.dotnet

Public Class ExceptionHelper

    Public Shared Sub dumpException(ByVal ex As Exception)
        dumpExceptionRecursively(ex, 0)
    End Sub

    Private Shared Sub dumpExceptionRecursively( _
            ByVal ex As Exception, ByVal indent As Integer)

        If ex Is Nothing Then Return

        Console.WriteLine(ex.Message)
        Console.WriteLine(ex.StackTrace)

        If ex.InnerException Is Nothing Then Return

        For i As Integer = 1 To indent
            Console.Write("*")
        Next
        Console.WriteLine(" INNER EXCEPTION >>>")
        dumpExceptionRecursively(ex.InnerException, indent + 3)
    End Sub


End Class

end namespace