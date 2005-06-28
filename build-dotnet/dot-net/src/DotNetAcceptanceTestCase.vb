Option Strict On

Imports System
Imports System.Configuration

Imports Spring.Context
Imports Spring.Context.Support
Imports log4net.Config

Imports org.nakedobjects.container.configuration
Imports org.nakedobjects
Imports org.nakedobjects.object
Imports org.nakedobjects.object.defaults
Imports org.nakedobjects.object.fixture
Imports org.nakedobjects.object.security
Imports org.nakedobjects.object.reflect

Imports org.nakedobjects.xat

Imports java.util

Imports junit.framework

Imports org.apache.log4j

Imports org.nakedobjects.viewer.skylark

Namespace org.nakedobjects.dotnet

    Public MustInherit Class DotNetAcceptanceTestCase
        Inherits TestCase

        Protected Shared NO_PARAMTERS(0) As TestNaked

#Region "constructors"
        Public Sub New()
            Me.new("(no name)")
        End Sub


        '*
        ' Looks up container using "SdmBusinessObjectContainer", and test object
        ' factory using "TestObjectFactory".
        Public Sub New(ByVal name As String)
            Me.New(name, "SdmBusinessObjectContainer", "TestObjectFactory", "objects.xml")
        End Sub

        '*
        ' Looks up container using "SdmBusinessObjectContainer", and test object
        ' factory using "TestObjectFactory".
        Public Sub New(ByVal name As String, ByVal springConfigFile As String)
            Me.New(name, "SdmBusinessObjectContainer", "TestObjectFactory", springConfigFile)
        End Sub

        Public Sub New(ByVal name As String, _
                ByVal containerId As String, ByVal testObjectFactoryId As String, _
                ByVal springConfigFile As String)
            MyBase.new(name)
            Me.myContainerId = containerId
            Me.myTestObjectFactoryId = testObjectFactoryId
            Me.mySpringConfigFile = springConfigFile
        End Sub


        Private myContainerId As String
        Private myTestObjectFactoryId As String
        Private mySpringConfigFile As String

        Private time As Double
#End Region

#Region "setUp() and tearDown()"
        '*
        ' Subclasses can override, but must delegate upwards (using MyBase) to
        ' setup the No framework itself.
        Public Overrides Sub setUp()

            Try
                org.apache.log4j.BasicConfigurator.configure()

                LogManager.getRootLogger().setLevel(Level.ERROR)

                initSpringContext()

                startLogging()

                Dim rf As ReflectorFactory = _
                DirectCast(myCtx.GetObject("SdmReflectorFactory"), ReflectorFactory)

                Dim factory As PojoAdapterFactoryImpl = New PojoAdapterFactoryImpl
                NakedObjects.setAdapterFactory(factory)
                factory.setPojoAdapterHash(New PojoAdapterHashImpl)
                factory.setReflectorFactory(rf)


                initTestObjectFactory(myTestObjectFactoryId)

                initContainer(myContainerId)

                setUpContainer()
                setUpFixtures()
                installFixtures()

                setUpTestClasses()
            Catch ex As Exception
                ExceptionHelper.dumpException(ex)
                Throw ex
            End Try

        End Sub



        '*
        ' Tears down the session, factory and documentor.
        '
        ' <p>
        ' A new Spring context is created in each setup, so the old NOF container
        ' is simply discarded through garbage collection.
        '
        ' <p>
        ' Subclasses should override if any static (shared) variables are setup, 
        ' blanking them out.  They should then delegate upwards using
        ' <code>MyBase.tearDown()</code>.
        '
        Public Overrides Sub tearDown()
            stopDocumenting()

            NakedObjects.shutdown()
            'NakedObjects.getObjectManager().shutdown()
            'NakedObjects.setObjectManager(Nothing)
            'NakedObjects.getPojoAdapterFactory().shutdown()
            'NakedObjects.setPojoAdapterFactory(Nothing)
            'NakedObjects.getSpecificationLoader().shutdown()
            'NakedObjects.setSpecificationLoader(Nothing)
            'NakedObjects.setConfiguration(Nothing)
            'NakedObjects.setSession(Nothing)

            myTestObjectFactory.testEnding()
            myDocumentor.stop()
            myDocumentor = Nothing

            myContainerId = Nothing
            myContainer = Nothing
            
            myCtx.Dispose()
            myCtx = Nothing

            myClasses.clear()
            myClasses = Nothing

            myTestObjectFactoryId = Nothing
            myTestObjectFactory = Nothing

            mySpringConfigFile = Nothing
            
            
        End Sub
#End Region

#Region "Helper methods to bootstrap NOF"
        Private myCtx As IConfigurableApplicationContext
        '*
        ' Instantiate from XML file rather than App.Config since need a complete
        ' new container each time.
        '
        ' <p>
        ' Also, get the opportunity to apply XSD schema files.
        Private Sub initSpringContext()
            myCtx = New XmlApplicationContext(mySpringConfigFile)
        End Sub

        Private Sub startLogging()
            PropertyConfigurator.configure( _
                NakedObjects.getConfiguration().getProperties("log4j"))
            Logger.getLogger( _
                java.lang.Class.FromType(GetType(AcceptanceTestCase))). _
                    debug("XAT Logging enabled - new test: " + getName())
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



        Private myTestObjectFactory As TestObjectFactory
        Private myDocumentor As Documentor
        '*
        ' Obtains an implementation of the TestObjectFactory.
        '
        Private Sub initTestObjectFactory(ByVal testObjectFactoryId As String)
            myTestObjectFactory = CType(myCtx.GetObject(testObjectFactoryId), _
                    TestObjectFactory)

            myDocumentor = myTestObjectFactory.getDocumentor()
            myDocumentor.start()

            Dim className As String = Me.getType().FullName
            Dim methodName As String = getName().Substring(4)
            myTestObjectFactory.testStarting(className, methodName)

        End Sub




        Private myClasses As Hashtable = New Hashtable
        Private Sub setUpTestClasses()

            Dim noSpecs As NakedObjectSpecification() = _
                NakedObjects.getSpecificationLoader().getAllSpecifications()

            For Each noSpec As NakedObjectSpecification In noSpecs
                Dim noSpecFullName As String = noSpec.getFullName()
                If noSpec.isObject() Then
                    Dim cls As NakedClass = New NakedClass(noSpec.getFullName())
                    Dim view As TestClass = _
                        myTestObjectFactory.createTestClass(cls)
                    myClasses.put(noSpec.getFullName().ToLower(), view)
                End If
            Next

        End Sub
#End Region

#Region "Methods for subclass to control documenting"
        Protected Sub startDocumenting()
            myDocumentor.start()
        End Sub

        '*
        ' Gives a story a subtitle in the script documentation.
        '
        Protected Sub title(ByVal text As String)
            myDocumentor.title(text)
        End Sub

        '*
        ' Gives a story a subtitle in the script documentation.
        '
        Protected Sub subtitle(ByVal text As String)
            myDocumentor.subtitle(text)
        End Sub

        Protected Sub firstStep()
            startDocumenting()
            nextStep()
        End Sub

        Protected Sub firstStep(ByVal text As String)
            startDocumenting()
            nextStep(text)
        End Sub


        '*
        ' Marks the start of a new step within a story. Adds the specified text to
        ' the script documentation, which will then be followed by the generated
        ' text from the action methods.
        '
        Protected Sub nextStep(ByVal text As String)
            myDocumentor.step(text)
        End Sub


        '*
        ' Marks the start of a new step within a story.
        Protected Sub nextStep()
            myDocumentor.step("")
        End Sub


        Protected Sub append(ByVal text As String)
            docln(text)
        End Sub

        Protected Sub note(ByVal text As String)
            docln(text)
        End Sub


        Protected Sub stopDocumenting()
            myDocumentor.stop()
        End Sub

#Region "helper methods"

        Private Sub docln(ByVal str As String)
            myDocumentor.docln(str)
        End Sub
#End Region


#End Region

#Region "Methods to allow subclass to setup test"
        '*
        ' Called before setUpFixtures()
        Protected MustOverride Sub setUpContainer()

        '*
        ' Called before setUpContainer()
        Protected MustOverride Sub setUpFixtures()

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

        '*
        ' If a class hasn't been previously registered (using
        ' <code>ExplorationSetUp#registerType(Type)</code>) then the function will
        ' simply return <code>nothing</code>.
        '
        ' <h3>Historical Note</h3>
        ' We used to throw an exception, but a null pointer later on will be
        ' good enough to indicate to the programmer that they've forgotten to do
        ' something.
        Protected Overloads Function getTestClass(ByVal systype As Type) _
                As TestClass

            Dim name As String = systype.FullName
            Dim view As TestClass = _
                CType(myClasses.get(name.ToLower()), TestClass)

            Return view
        End Function

#End Region

#Region "Methods for subclasses to actually implement their tests"
        Public Function createParameterTestValue(ByVal value As Object) As TestValue
            Return myTestObjectFactory.createParamerTestValue(value)
        End Function

        Public Function createNullParameter(ByVal systype As Type) As TestNaked
            Return New TestNakedNullParameter(systype.FullName)
        End Function

        '*
        ' All parameters must be either TestObject (representing a referenced
        ' NakedObject, or a NakedValue.
        '
        ' <p>
        ' Behaviour otherwise is not defined.
        '
        Protected Function params( _
            ByVal p0 As Object) As TestNaked()

            Dim param(0) As TestNaked
            param(0) = asTestNaked(p0)

            Return param

        End Function

        '*
        ' All parameters must be either TestObject (representing a referenced
        ' NakedObject, or a NakedValue.
        '
        ' <p>
        ' Behaviour otherwise is not defined.
        '
        Protected Function params( _
            ByVal p0 As Object, _
            ByVal p1 As Object) As TestNaked()

            Dim param(1) As TestNaked
            param(0) = asTestNaked(p0)
            param(1) = asTestNaked(p1)

            Return param

        End Function

        '*
        ' All parameters must be either TestObject (representing a referenced
        ' NakedObject, or a NakedValue.
        '
        ' <p>
        ' Behaviour otherwise is not defined.
        '
        Protected Function params( _
            ByVal p0 As Object, _
            ByVal p1 As Object, _
            ByVal p2 As Object) As TestNaked()

            Dim param(2) As TestNaked
            param(0) = asTestNaked(p0)
            param(1) = asTestNaked(p1)
            param(2) = asTestNaked(p2)

            Return param

        End Function

        '*
        ' All parameters must be either TestObject (representing a referenced
        ' NakedObject, or a NakedValue.
        '
        ' <p>
        ' Behaviour otherwise is not defined.
        '
        Protected Function params( _
            ByVal p0 As Object, _
            ByVal p1 As Object, _
            ByVal p2 As Object, _
            ByVal p3 As Object) As TestNaked()

            Dim param(3) As TestNaked
            param(0) = asTestNaked(p0)
            param(1) = asTestNaked(p1)
            param(2) = asTestNaked(p2)
            param(3) = asTestNaked(p3)

            Return param

        End Function

        '*
        ' All parameters must be either TestObject (representing a referenced
        ' NakedObject, or a NakedValue.
        '
        ' <p>
        ' Behaviour otherwise is not defined.
        '
        Protected Function params( _
            ByVal p0 As Object, _
            ByVal p1 As Object, _
            ByVal p2 As Object, _
            ByVal p3 As Object, _
            ByVal p4 As Object) As TestNaked()

            Dim param(4) As TestNaked
            param(0) = asTestNaked(p0)
            param(1) = asTestNaked(p1)
            param(2) = asTestNaked(p2)
            param(3) = asTestNaked(p3)
            param(4) = asTestNaked(p4)

            Return param

        End Function

        '*
        ' All parameters must be either TestObject (representing a referenced
        ' NakedObject, or a NakedValue.
        '
        ' <p>
        ' Behaviour otherwise is not defined.
        '
        Protected Function params( _
            ByVal p0 As Object, _
            ByVal p1 As Object, _
            ByVal p2 As Object, _
            ByVal p3 As Object, _
            ByVal p4 As Object, _
            ByVal p5 As Object) As TestNaked()

            Dim param(5) As TestNaked
            param(0) = asTestNaked(p0)
            param(1) = asTestNaked(p1)
            param(2) = asTestNaked(p2)
            param(3) = asTestNaked(p3)
            param(4) = asTestNaked(p4)
            param(5) = asTestNaked(p5)

            Return param

        End Function

        '*
        ' All parameters must be either TestObject (representing a referenced
        ' NakedObject, or a NakedValue.
        '
        ' <p>
        ' Behaviour otherwise is not defined.
        '
        Protected Function params( _
            ByVal p0 As Object, _
            ByVal p1 As Object, _
            ByVal p2 As Object, _
            ByVal p3 As Object, _
            ByVal p4 As Object, _
            ByVal p5 As Object, _
            ByVal p6 As Object) As TestNaked()

            Dim param(6) As TestNaked
            param(0) = asTestNaked(p0)
            param(1) = asTestNaked(p1)
            param(2) = asTestNaked(p2)
            param(3) = asTestNaked(p3)
            param(4) = asTestNaked(p4)
            param(5) = asTestNaked(p5)
            param(6) = asTestNaked(p6)

            Return param

        End Function

        '*
        ' All parameters must be either TestObject (representing a referenced
        ' NakedObject, or a NakedValue.
        '
        ' <p>
        ' Behaviour otherwise is not defined.
        '
        Protected Function params( _
            ByVal p0 As Object, _
            ByVal p1 As Object, _
            ByVal p2 As Object, _
            ByVal p3 As Object, _
            ByVal p4 As Object, _
            ByVal p5 As Object, _
            ByVal p6 As Object, _
            ByVal p7 As Object) As TestNaked()

            Dim param(7) As TestNaked
            param(0) = asTestNaked(p0)
            param(1) = asTestNaked(p1)
            param(2) = asTestNaked(p2)
            param(3) = asTestNaked(p3)
            param(4) = asTestNaked(p4)
            param(5) = asTestNaked(p5)
            param(6) = asTestNaked(p6)
            param(7) = asTestNaked(p7)

            Return param

        End Function

        Protected Overridable Function asTestNaked(ByVal o As Object) As TestNaked
            If TypeOf o Is TestNaked Then Return CType(o, TestNaked)
            If TypeOf o Is TestObject Then Return CType(o, TestObject)
            Return Nothing
        End Function

#End Region

#Region "Helper methods: Command line parsing"
        '*
        ' Check if command line argument specifies if the XAT suite is to be
        ' run in text mode. Typically this is used in the automated XAT from
        ' the build process
        Protected Shared Function isTextRunnerType( _
                ByVal CommandLineArguments As String()) As Boolean
            Dim CommandLineArg As String
            Dim CommandLineArgValue As String

            ' parse the command line for the run mode parameter
            For Each CommandLineArg In CommandLineArguments
                If CommandLineArg.Length > 3 Then
                    If CommandLineArg.IndexOf("/t:") = 0 Then
                        ' get command line argument value
                        CommandLineArgValue = CommandLineArg.Substring(3)
                        ' set as true if text mode. default is AWT mode
                        Return (CommandLineArgValue.IndexOf("TextMode") = 0)
                    End If
                End If
            Next
            Return False

        End Function
#End Region

    End Class

End Namespace