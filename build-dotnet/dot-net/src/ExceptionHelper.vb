Imports System
Imports System.Text



namespace org.nakedobjects.dotnet

Public Class ExceptionHelper
        Public Shared Sub dumpException(ByVal ex As Exception)
            Dim trace as String
            trace = exceptionAsString(ex)
            org.nakedobjects.utility.ExceptionHelper.submitLog(ex.Message, trace)
            Console.WriteLine(trace)
        End Sub
        
        Public Shared Function exceptionAsString(ByVal ex As Exception) as String
            Dim trace As StringBuilder = New StringBuilder
            dumpExceptionRecursively(trace, ex, 0)
            return trace.ToString()
        End Function

        Private Shared Sub dumpExceptionRecursively( _
            ByVal trace As StringBuilder, ByVal ex As Exception, ByVal indent As Integer)

            If ex Is Nothing Then Return

            trace.Append(ex.Message)
            trace.Append(Environment.NewLine)
            trace.Append(ex.StackTrace)
            trace.Append(Environment.NewLine)

            If ex.InnerException Is Nothing Then Return

            For i As Integer = 1 To indent
                trace.Append("* ")
            Next
            trace.Append("INNER EXCEPTION >>>")
            trace.Append(Environment.NewLine)
            dumpExceptionRecursively(trace, ex.InnerException, indent + 3)
        End Sub


End Class

end namespace