Imports System
Imports System.Collections


Imports org.nakedobjects.object


'*
' DotNet adapter for AbstractUserContext: defines the classes (icons) to
' display on the root context (the user's workspace) (AbstractUserContext).
'
' <p>
' In addition to just adapting AbstractUserContext, the root classes can be
' defined either programmatically or using dependency injection.
'
' <p>
' NakedObjects is written in Java/J#, which means that whenever it deals with
' code reflectively, it uses java.lang.Class.  For .NET programmers, it is more
' natural to use System.Type.
'
' <p>
' This class insulates .NET developers from the NakedObjects implementation
' language by representing the base class functionality in terms of methods
' that accept System.Type.
'
namespace org.nakedobjects.dotnet

Public Class DotNetUserContext
    Inherits org.nakedobjects.object.ApplicationContext


    Public Sub New()
        registerRootClasses(myRootClassTypes)
    End Sub


#Region "property: rootClasses"
    '*
    ' Holds the <i>Types of</i> root classes to register, as provided 
    ' programmatically in <code>registerRootClasses</code>
    Private myRootClassTypes As IList = New ArrayList
    '*
    ' Subclasses can register classes for display on the root context here,
    ' by adding the types to the supplied IList.
    '
    ' <p>
    ' For example:
    ' <code>
    ' classes.add(GetType(ecs.bom.Customer))
    ' classes.add(GetType(ecs.bom.Booking))
    ' classes.add(GetType(ecs.bom.City))
    ' </code>
    '
    ' <p>
    ' This implementation does nothing and is intended to be overridden.
    ' Alternatively root classes can be provided through dependency
    ' injection.
    Protected Overridable Sub registerRootClasses(ByVal classes As IList)

    End Sub


    '*
    ' Holds the <i>names of</i> root classes to register, as provided by
    ' dependency injection.
    Private myRootClassNames As IList = New ArrayList
    '*
    ' Allow root classes (or rather, their names( to be supplied through
    ' dependency injection.
    '
    ' <p>
    ' For example, in Spring config file:
    ' <code>
    ' &lt;object id="Context"
    '            type="org.nakedobjects.context.ApplicationContext, nakedobjects.net"&gt;
    '   &lt;property name="RootClasses"&gt;
    '     &lt;list&gt;
    '       &lt;value&gt;ecs.bom.Customer&lt;/value&gt;
    '       &lt;value&gt;ecs.bom.Booking&lt;/value&gt;
    '       &lt;value&gt;ecs.bom.City&lt;/value&gt;
    '     &lt;/list&gt;
    '   &lt;/property&gt;
    ' &lt;/object&gt;
    ' </code>
    Public WriteOnly Property RootClasses() As IList
        Set(ByVal Value As IList)
            myRootClassNames = Value
        End Set
    End Property

#End Region

#Region "property: name"
    Public Overrides Function name() As String
        Return "Naked Objects"
    End Function
    Public Overridable Function title() As String
        Return name()
    End Function
#End Region



    '*
    ' Called by framework, registers the root classes as previously set up
    ' in myRootClasses (either programmatically or by dependency injection)
    ' with the framework.
    Public Sub created()
        If myRootClassTypes.Count = 0 AndAlso _
           myRootClassNames.Count = 0 Then
            Throw New ApplicationException("No root classes registered.")
        End If
        ensureClassTypesAreValid()
        ensureClassNamesAreValid()
        For Each t As Type In myRootClassTypes
            addClass(t)
        Next
        For Each typeName As String In myRootClassNames
            addClass(New TypeHelper().type(typeName))
        Next
    End Sub



#Region "Helper methods"
    Private Sub ensureClassTypesAreValid()
        If Not checkListContainsOnly(myRootClassTypes, GetType(Type)) Then
            Throw New ApplicationException("Only System.Types should be added to class list (in registerRootClasses)")
        End If
    End Sub

    '*
    ' For any and all class names, make sure there is a loadable Type with 
    ' corresponding FullName.
    Private Sub ensureClassNamesAreValid()
        If myRootClassNames.Count <> 0 Then
            For Each className As String In myRootClassNames
                If New TypeHelper().type(className) Is Nothing Then
                    Throw New ApplicationException("Class '" & className & "' cannot be found")
                End If
            Next
        End If
    End Sub


    Private Function checkListContainsOnly( _
            ByVal list As IList, ByVal type As Type) _
            As Boolean
        For Each o As Object In list
            If Not type.IsAssignableFrom(o.GetType()) Then Return False
        Next
        Return True
    End Function
#End Region


#Region "Override base class"
    Protected Overridable Overloads Sub addClass(ByVal systype As Type)
        MyBase.addClass(systype.FullName)
    End Sub
    Public Overloads Overrides Function addClass(ByVal className As String) As NakedClass
        Throw New ApplicationException("Specify classes either programmatically in registerRootClasses or through dependency injection of the RootClasses property")
    End Function
#End Region


    Public Shared Function singleName() As String
        Return "Application"
    End Function


End Class


end namespace