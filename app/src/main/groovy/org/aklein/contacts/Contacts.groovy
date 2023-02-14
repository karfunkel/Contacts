package org.aklein.contacts

import groovy.util.logging.Slf4j
import org.aklein.contacts.adapter.Adapter
import org.aklein.contacts.adapter.CSV
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.ITypeConverter
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import picocli.CommandLine.Spec

import java.util.prefs.Preferences

import static picocli.CommandLine.Model.CommandSpec

@Command(
        name = "Contacts",
        mixinStandardHelpOptions = true,
        version = "0.9.0",
        description = "Select and execute reports on the Google org.aklein.contacts.Contacts Export (contacts.csv)"
)
@Slf4j
class Contacts implements Runnable {
    @Spec
    CommandSpec spec

    File input = null

    @Parameters(index = "0", arity = "1", paramLabel = "INPUT", defaultValue = "System.in", showDefaultValue = CommandLine.Help.Visibility.ALWAYS, description = "The input file to read the data from", converter = [
            {
                { String name ->
                    if (name == "System.in") {
                        name = System.in.withReader {
                            def reader = new BufferedReader(it)
                            reader.ready() ? reader.text : null
                        }
                    }
                    return name ? new File(name) : null
                } as ITypeConverter
            }
    ])
    void setInput(File input) {
        if (input == null || input.exists())
            this.input = input
        else
            throw new CommandLine.ParameterException(spec.commandLine(), "Invalid value for option INPUT: File '$input' does not exist.")
    }

    @Option(names = ["-a", "--adapter"], description = "Input adapter", defaultValue = "CSV", showDefaultValue = CommandLine.Help.Visibility.ALWAYS, converter = [
            {
                {
                    Class cls
                    ClassLoader classLoader = Contacts.classLoader
                    if (it.contains('.')) {
                        try {
                            cls = classLoader.loadClass(it)
                        } catch (ClassNotFoundException e) {
                            // empty
                        }
                    }
                    if (!cls) {
                        try {
                            cls = classLoader.loadClass("org.aklein.contacts.adapter.$it")
                        } catch (ClassNotFoundException e) {
                            throw new CommandLine.TypeConversionException("Class org.aklein.contacts.adapter.$it not found.")
                        }
                    }
                    if (!Adapter.isAssignableFrom(cls)) {
                        throw new CommandLine.TypeConversionException("Class '$cls.canonicalName' does not implement $Adapter.canonicalName")
                    }
                    return cls.newInstance()
                } as ITypeConverter
            }
    ])
    Adapter adapter = new CSV()

    File reportDir = new File('reports')

    @Option(names = ["-r", "--reportDir"], description = "Directory containing the reports", defaultValue = "reports", showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
    void setReportDir(File reportDir) {
        if (!reportDir.exists())
            reportDir.mkdirs()
        if (!reportDir.isDirectory())
            throw new CommandLine.ParameterException(spec.commandLine(), "Invalid value for option '--reportDir': '$reportDir' is not a directory.")
        this.reportDir = reportDir
    }

    @Option(names = ["-l", "--language"], description = "Language", defaultValue = "de", showDefaultValue = CommandLine.Help.Visibility.ALWAYS, converter = [{ { new Locale(*it.split('_')) } as ITypeConverter }])
    Locale language = Locale.GERMAN

    @Option(names = "-d", defaultValue = "false", description = "delete input when closing")
    boolean deleteInput = false

    Preferences prefs = Preferences.userNodeForPackage(this.getClass())

    void run() {
        new Main(input, adapter, reportDir, language, prefs, spec.commandLine(), deleteInput: deleteInput)
    }

    static void main(String[] args) {
        new CommandLine(new Contacts()).execute(args)
    }
}
