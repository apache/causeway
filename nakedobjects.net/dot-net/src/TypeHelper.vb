Imports System
Imports System.Reflection

namespace org.nakedobjects.dotnet

'*
' Returns type with given name, cf Class#forName().
'
' <p>
' Equivalent of Type.GetType(String), however searches all assemblies (DLLs) loaded, 
' rather than searching only current assembly.
'
Public Class TypeHelper

    '*
    ' Loops over all loaded assemblies and attempts to find type with given className
    ' from each such assembly.
    Public Function type(ByVal className As String) As type

        'For Each a As [Assembly] In AppDomain.CurrentDomain.GetAssemblies 
        '    Dim curType As type = a.GetType(className)
        '    If Not curType Is Nothing Then Return curType
        'Next
        'Return Nothing

        ' Need to use GetEntryAssembly as AppDomain.CurrentDomain.GetAssemblies 
        ' does not load all the required Assemblies e.g. sdm.cho.tests
        For Each assmName As AssemblyName In _
                [Assembly].GetEntryAssembly.GetReferencedAssemblies
            Dim assm As [Assembly] = [Assembly].Load(assmName)
            Dim thisType As type = assm.GetType(className)
            If Not thisType Is Nothing Then Return thisType
        Next
        Return Nothing

    End Function

End Class

end namespace