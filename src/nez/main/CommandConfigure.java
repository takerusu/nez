package nez.main;

import java.io.IOException;

import nez.Grammar;
import nez.Production;
import nez.SourceContext;
import nez.expr.GrammarChecker;
import nez.expr.NezParser;
import nez.expr.NezParserCombinator;
import nez.util.ConsoleUtils;
import nez.util.StringUtils;
import nez.util.UList;
import nez.util.UMap;

public class CommandConfigure {
//	// -X specified
//	private static Class<?> OutputWriterClass = null;

	public String CommandName = "shell";

	// -p konoha.nez
	public String GrammarFile = null; // default

	// -e, --expr regular expression
	public String Expression = null;

	// -s, --start
	public String StartingPoint = "File";  // default
	
	// -i, --input
	public String InputFileName = null;
	public UList<String> InputFileLists = null;

	// -t, --text
	public String InputText = null;

	// -o, --output
	public String OutputFileName = null;

	// -W
	public int WarningLevel = 1;
	
	// -g
	public int DebugLevel = 1;
	
	// --verbose
	public boolean VerboseMode    = false;
	
	// -O
	public int OptimizationLevel = 2;

	
	void showUsage(String Message) {
		ConsoleUtils.println("nez <command> optional files");
		ConsoleUtils.println("  -p | --peg <filename>      Specify an PEGs grammar file");
		ConsoleUtils.println("  -i | --input <filename>    Specify an input file");
		ConsoleUtils.println("  -t | --text  <string>     Specify an input text");
		ConsoleUtils.println("  -o | --output <filename>   Specify an output file");
//		ConsoleUtils.println("  -t | --type <filename>     Specify an output type");
		ConsoleUtils.println("  -s | --start <NAME>        Specify Non-Terminal as the starting point (default: File)");
		ConsoleUtils.println("  -W<num>                    Warning Level (default:1)");
		ConsoleUtils.println("  -O<num>                    Optimization Level (default:2)");
		ConsoleUtils.println("  -g                         Debug Level");
		ConsoleUtils.println("   -Xclassic                 Running on the classic recusive decent parsing");
		ConsoleUtils.println("  --memo:x                   Memo configuration");
		ConsoleUtils.println("     none|packrat|window|slide|notrace");
		ConsoleUtils.println("  --memo:<num>               Expected backtrack distance (default: 256)");
		ConsoleUtils.println("  --verbose                  Printing Debug infomation");
		ConsoleUtils.println("  --verbose:memo             Printing Memoization information");
		ConsoleUtils.println("  -X <class>                 Specify an extension class");
		ConsoleUtils.println("");
		ConsoleUtils.println("The most commonly used nez commands are:");
		ConsoleUtils.println("  parse        Parse -i input or -s string to -o output");
		ConsoleUtils.println("  check        Parse -i input or -s string");
		ConsoleUtils.println("  shell        Try parsing in an interactive way");
		ConsoleUtils.println("  rel          Convert -f file to relations (csv file)");
		ConsoleUtils.println("  nezex        Convert -i regex to peg");
		ConsoleUtils.println("  conv         Convert PEG4d rules to the specified format in -o");
		ConsoleUtils.println("  find         Search nonterminals that can match inputs");
		ConsoleUtils.exit(0, Message);
	}
	
	void parseCommandOption(String[] args) {
		int index = 0;
		if(args.length > 0) {
			if(!args[0].startsWith("-")) {
				CommandName = args[0];
				index = 1;
			}
		}
		while (index < args.length) {
			String argument = args[index];
			if (!argument.startsWith("-")) {
				break;
			}
			index = index + 1;
			if (argument.equals("-X") && (index < args.length)) {
				try {
					Class<?> c = Class.forName(args[index]);
//					if(ParsingWriter.class.isAssignableFrom(c)) {
//						OutputWriterClass = c;
//					}
				} catch (ClassNotFoundException e) {
					ConsoleUtils.exit(1, "-X specified class is not found: " + args[index]);
				}
				index = index + 1;
			}
			else if ((argument.equals("-p") || argument.equals("--peg")) && (index < args.length)) {
				GrammarFile = args[index];
				index = index + 1;
			}
			else if ((argument.equals("-t") || argument.equals("--text")) && (index < args.length)) {
				InputText = args[index];
				index = index + 1;
			}
			else if ((argument.equals("-i") || argument.equals("--input")) && (index < args.length)) {
				InputFileName = args[index];
				index = index + 1;
			}
			else if ((argument.equals("-o") || argument.equals("--output")) && (index < args.length)) {
				OutputFileName = args[index];
				index = index + 1;
			}
			else if ((argument.equals("-s") || argument.equals("--start")) && (index < args.length)) {
				StartingPoint = args[index];
				index = index + 1;
			}
			else if (argument.startsWith("-O")) {
				OptimizationLevel = StringUtils.parseInt(argument.substring(2), 2);
			}
			else if (argument.startsWith("-W")) {
				WarningLevel = StringUtils.parseInt(argument.substring(2), 2);
			}
			else if (argument.startsWith("-g")) {
				DebugLevel = StringUtils.parseInt(argument.substring(2), 1);
			}
			else if (argument.startsWith("-g")) {
				DebugLevel = StringUtils.parseInt(argument.substring(2), 1);
			}
//			else if(argument.startsWith("--memo")) {
//				if(argument.equals("--memo:none")) {
//					MemoizationManager.NoMemo = true;
//				}
//				else if(argument.equals("--memo:packrat")) {
//					MemoizationManager.PackratParsing = true;
//				}
//				else if(argument.equals("--memo:window")) {
//					MemoizationManager.SlidingWindowParsing = true;
//				}
//				else if(argument.equals("--memo:slide")) {
//					MemoizationManager.SlidingLinkedParsing = true;
//				}
//				else if(argument.equals("--memo:notrace")) {
//					MemoizationManager.Tracing = false;
//				}
//				else {
//					int distance = Utils.parseInt(argument.substring(7), -1);
//					if(distance >= 0) {
//						MemoizationManager.BacktrackBufferSize  = distance;
//					}
//					else {
//						showUsage("unknown option: " + argument);
//					}
//				}
//			}
			else if(argument.startsWith("-Xconfig")) {
				ProductionOption = StringUtils.parseInt(argument.substring(9), Production.DefaultOption);
				Verbose.println("configuration: " + Production.stringfyOption(ProductionOption, ", "));
			}
			else if(argument.startsWith("-Xrec")) {
				RecorderFileName = "nezrec.csv";
				if(argument.endsWith(".csv")) {
					RecorderFileName = argument.substring(6);
				}
				Verbose.println("recording " + RecorderFileName);
			}
			else if(argument.startsWith("--verbose")) {
				if(argument.equals("--verbose:memo")) {
					Verbose.PackratParsing = true;
				}
				else if(argument.equals("--verbose:vm")) {
					Verbose.VirtualMachine = true;
				}
				else if(argument.equals("--verbose:none")) {
					Verbose.General = false;
				}
				else {
					Verbose.setAll();
					Verbose.println("unknown verbose option: " + argument);
				}
			}
			else {
				this.showUsage("unknown option: " + argument);
			}
		}
//		if(index < args.length) {
//			FileList = new String[args.length - index];
//			System.arraycopy(args, index, FileList, 0, FileList.length);
//		}
//		if(GrammarFile == null) {
//			if(InputFileName != null) {
//				GrammarFile = guessGrammarFile(InputFileName);
//			}
//		}
//		if(InputFileName == null && InputString == null && !PegVMByteCodeGeneration) {
//			System.out.println("unspecified inputs: invoking interactive shell");
//			Command = "shell";
//		}
//		if(OutputWriterClass == null) {
//			OutputWriterClass = ParsingObjectWriter.class;
//		}
	}

	private static UMap<Command> commandTable = new UMap<Command>();
	private static void addCommand(String name, Command com) {
		commandTable.put(name, com);
	}
	static {
		addCommand("parse", new ParseCommand());
	}
	
	public final Command getCommand() {
		Command com = commandTable.get(this.CommandName);
		if(com == null) {
			this.showUsage("unknown command: " + this.CommandName);
		}
		return com;
	}
	
	public final Grammar getGrammar() {
		if(GrammarFile != null) {
			try {
				NezParser p = new NezParser();
				return p.load(SourceContext.loadSource(GrammarFile), new GrammarChecker(this.OptimizationLevel));
			} catch (IOException e) {
				ConsoleUtils.exit(1, "cannot open " + GrammarFile + "; " + e.getMessage());
			}
		}
		ConsoleUtils.println("unspecifed grammar");
		return NezParserCombinator.newGrammar();
	}

	public final Production getProduction(String start, int option) {
		if(start == null) {
			start = this.StartingPoint;
		}
		return getGrammar().getProduction(start, option);
	}

	private int ProductionOption = Production.DefaultOption;
	
	public final Production getProduction(String start) {
		if(start == null) {
			start = this.StartingPoint;
		}
		return getGrammar().getProduction(start, ProductionOption);
	}

	private boolean ShellMode = true;
	
	public final boolean hasInput() {
		if(ShellMode && this.InputText == null && this.InputFileName == null) {
			this.InputText = Command.readMultiLine(">>> ", "... ");
			return this.InputText != null;
		}
		ShellMode = false;
		return this.InputText != null || this.InputFileName != null;
	}
	
	public final SourceContext getInputSourceContext() {
		if(this.InputText != null) {
			String text = this.InputText;
			this.InputText = null;
			return SourceContext.newStringSourceContext(text);
		}
		if(this.InputFileName != null) {
			String f = this.InputFileName;
			this.InputFileName = null;
			try {
				return SourceContext.loadSource(f);
			} catch (IOException e) {
				ConsoleUtils.exit(1, "cannot open: " + f);
			}
		}
		return SourceContext.newStringSourceContext(""); // empty input
	}

	public String getOutputFileName(SourceContext input) {
		return null;
	}

	public String RecorderFileName = null;

	public final Recorder getRecorder() {
		if(RecorderFileName != null) {
			Recorder rec = new Recorder(RecorderFileName);
			rec.setText("nez", Command.Version);
			rec.setText("config", Production.stringfyOption(ProductionOption, ";"));
			return rec;
		}
		return null;
	}

}
