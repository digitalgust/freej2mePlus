package org.recompile.mobile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Runtime bytecode injector for third-party MIDlet classes.
 * <p>
 * Rule format:
 * ENTRY_PRINT|<class>|<method>|<desc>|<template>
 * EXIT_PRINT  |<class>|<method>|<desc>|<template>
 * <p>
 * Templates support placeholders:
 * {n}      -> append argument n (any type)
 * {n[m]}   -> append element m of array argument n (array must be int[]/float[]/long[] etc.)
 * <p>
 * ENTRY_PRINT injects at method entry (arguments in original form).
 * EXIT_PRINT injects before every RETURN instruction (arguments hold their
 * current value at that point — e.g. after the method mutated them).
 * <p>
 * Examples:
 * ENTRY_PRINT|q|a|(FFFFFFFFF)V|q.a camera params: {0}, {1}, {2} | {3}, {4}, {5} | {6}, {7}, {8}
 * EXIT_PRINT|q|a|([F)V|q.a proj out: [{0[0]},{0[1]},{0[2]},{0[3]}]
 * <p>
 * Rules can be provided by:
 * 1. System property freej2me.bytecode.injection.rules
 * 2. System property freej2me.bytecode.injection.file
 * 3. A sidecar file next to the MIDlet jar: <midlet>.inject.txt
 * 4. A shared file in the same directory: freej2me-injections.txt
 */
public final class BytecodeInjectionTool {

    public static final String RULES_PROPERTY = "freej2me.bytecode.injection.rules";
    public static final String RULES_FILE_PROPERTY = "freej2me.bytecode.injection.file";

    private final List<MethodPrintRule> printRules = new ArrayList<MethodPrintRule>();
    private final Set<String> loadedSources = new HashSet<String>();

    public static BytecodeInjectionTool load(URL[] sourceUrls) {
        BytecodeInjectionTool tool = new BytecodeInjectionTool();
        tool.loadFromInlineProperty();
        tool.loadFromConfiguredFile();
        tool.loadFromSidecarFiles(sourceUrls);
        return tool;
    }

    public boolean hasRules() {
        return !printRules.isEmpty();
    }

    public MethodVisitor wrapMethod(String owner, int access, String name, String desc, MethodVisitor visitor) {
        if (visitor == null || owner == null || !hasRules()) {
            return visitor;
        }
        if ("<init>".equals(name) || "<clinit>".equals(name) || (access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) != 0) {
            return visitor;
        }
        boolean entry = false;
        boolean exit = false;
        String template = null;
        for (int i = 0; i < printRules.size(); i++) {
            MethodPrintRule rule = printRules.get(i);
            if (rule.matches(owner, name, desc)) {
                template = rule.template;
                if ("EXIT".equals(rule.pointcut)) {
                    exit = true;
                } else {
                    entry = true;
                }
            }
        }
        if (!entry && !exit) {
            return visitor;
        }
        System.out.println("[BytecodeInjection] Inject print (entry=" + entry + ", exit=" + exit + "): "
                + owner + "." + name + desc);
        return new PrintMethodVisitor(visitor, access, desc, template, entry, exit);
    }

    private void loadFromInlineProperty() {
        String rules = System.getProperty(RULES_PROPERTY, "").trim();
        if (rules.length() == 0) {
            return;
        }
        loadRules("system-property:" + RULES_PROPERTY, new BufferedReader(new StringReader(rules)));
    }

    private void loadFromConfiguredFile() {
        String path = System.getProperty(RULES_FILE_PROPERTY, "").trim();
        if (path.length() == 0) {
            return;
        }
        loadRules(new File(path));
    }

    private void loadFromSidecarFiles(URL[] sourceUrls) {
        if (sourceUrls == null) {
            return;
        }
        for (int i = 0; i < sourceUrls.length; i++) {
            File jarFile = toFile(sourceUrls[i]);
            if (jarFile == null) {
                continue;
            }
            loadRules(new File(jarFile.getAbsolutePath() + ".inject.txt"));
            File parent = jarFile.getParentFile();
            if (parent != null) {
                loadRules(new File(parent, "freej2me-injections.txt"));
            }
        }
    }

    private File toFile(URL url) {
        try {
            if (url == null || !"file".equalsIgnoreCase(url.getProtocol())) {
                return null;
            }
            String path = url.getPath();
            if (path == null || path.length() == 0) {
                path = url.getFile();
            }
            if (path == null || path.length() == 0) {
                return null;
            }
            path = decodeFileUrlPath(path);
            if (path.length() >= 3 && path.charAt(0) == '/' && path.charAt(2) == ':') {
                path = path.substring(1);
            }
            return new File(path);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String decodeFileUrlPath(String path) {
        StringBuilder out = new StringBuilder(path.length());
        for (int i = 0; i < path.length(); i++) {
            char ch = path.charAt(i);
            if (ch == '%' && i + 2 < path.length()) {
                int hi = hexValue(path.charAt(i + 1));
                int lo = hexValue(path.charAt(i + 2));
                if (hi >= 0 && lo >= 0) {
                    out.append((char) ((hi << 4) | lo));
                    i += 2;
                    continue;
                }
            }
            out.append(ch);
        }
        return out.toString();
    }

    private int hexValue(char ch) {
        if (ch >= '0' && ch <= '9') {
            return ch - '0';
        }
        if (ch >= 'a' && ch <= 'f') {
            return 10 + (ch - 'a');
        }
        if (ch >= 'A' && ch <= 'F') {
            return 10 + (ch - 'A');
        }
        return -1;
    }

    private void loadRules(File file) {
        if (file == null || !file.isFile()) {
            return;
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            loadRules(file.getAbsolutePath(), reader);
        } catch (Exception e) {
            System.out.println("[BytecodeInjection] Failed to load rules from " + file.getAbsolutePath());
            e.printStackTrace();
        } finally {
            closeQuietly(reader);
        }
    }

    private void loadRules(String source, BufferedReader reader) {
        if (!loadedSources.add(source)) {
            return;
        }
        try {
            String line;
            int lineNo = 0;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                parseRuleLine(source, lineNo, line);
            }
        } catch (Exception e) {
            System.out.println("[BytecodeInjection] Failed to parse rules from " + source);
            e.printStackTrace();
        }
    }

    private void parseRuleLine(String source, int lineNo, String rawLine) {
        String line = rawLine.trim();
        if (line.length() == 0 || line.startsWith("#") || line.startsWith("//")) {
            return;
        }
        String[] parts = line.split("\\|", 5);
        if (parts.length != 5) {
            System.out.println("[BytecodeInjection] Ignoring malformed rule at " + source + ":" + lineNo);
            return;
        }
        String type = parts[0].trim();
        String pointcut;
        if ("ENTRY_PRINT".equals(type)) {
            pointcut = "ENTRY";
        } else if ("EXIT_PRINT".equals(type)) {
            pointcut = "EXIT";
        } else {
            System.out.println("[BytecodeInjection] Unsupported rule type at " + source + ":" + lineNo + " -> " + type);
            return;
        }
        MethodPrintRule rule = new MethodPrintRule(
                pointcut,
                toInternalClassName(unescape(parts[1].trim())),
                unescape(parts[2].trim()),
                unescape(parts[3].trim()),
                unescape(parts[4]));
        printRules.add(rule);
        System.out.println("[BytecodeInjection] Loaded rule: " + rule.owner + "." + rule.name + rule.desc);
    }

    private String toInternalClassName(String value) {
        return value.replace('.', '/');
    }

    private String unescape(String value) {
        StringBuilder out = new StringBuilder(value.length());
        boolean escaped = false;
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (escaped) {
                switch (ch) {
                    case 'n':
                        out.append('\n');
                        break;
                    case 'r':
                        out.append('\r');
                        break;
                    case 't':
                        out.append('\t');
                        break;
                    default:
                        out.append(ch);
                        break;
                }
                escaped = false;
            } else if (ch == '\\') {
                escaped = true;
            } else {
                out.append(ch);
            }
        }
        if (escaped) {
            out.append('\\');
        }
        return out.toString();
    }

    private void closeQuietly(BufferedReader reader) {
        if (reader == null) {
            return;
        }
        try {
            reader.close();
        } catch (IOException ignored) {
        }
    }

    private static final class MethodPrintRule {
        private final String pointcut;
        private final String owner;
        private final String name;
        private final String desc;
        private final String template;

        private MethodPrintRule(String pointcut, String owner, String name, String desc, String template) {
            this.pointcut = pointcut;
            this.owner = owner;
            this.name = name;
            this.desc = desc;
            this.template = template;
        }

        private boolean matches(String owner, String name, String desc) {
            return this.owner.equals(owner) && this.name.equals(name) && this.desc.equals(desc);
        }
    }

    private static final class PrintMethodVisitor extends MethodAdapter implements Opcodes {
        private final int access;
        private final String desc;
        private final String template;
        private final boolean atEntry;
        private final boolean atExit;

        private PrintMethodVisitor(MethodVisitor visitor, int access, String desc,
                                   String template, boolean atEntry, boolean atExit) {
            super(visitor);
            this.access = access;
            this.desc = desc;
            this.template = template;
            this.atEntry = atEntry;
            this.atExit = atExit;
        }

        public void visitCode() {
            super.visitCode();
            if (atEntry) {
                injectPrint();
            }
        }

        public void visitInsn(int opcode) {
            if (atExit) {
                if (opcode == Opcodes.RETURN) {
                    injectPrint();
                } else if (opcode == Opcodes.IRETURN && template.indexOf("{ret}") >= 0) {
                    // print int return value: stack currently holds [ret]; dup it.
                    mv.visitInsn(Opcodes.DUP);
                    injectReturnPrint();
                }
            }
            super.visitInsn(opcode);
        }

        // Prints the int currently on top of the stack (the dup'd return value),
        // then leaves the original return value intact for the real IRETURN.
        private void injectReturnPrint() {
            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitInsn(Opcodes.SWAP);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V");
        }

        private void injectPrint() {
            Type[] argumentTypes = Type.getArgumentTypes(desc);

            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitTypeInsn(NEW, "java/lang/StringBuffer");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuffer", "<init>", "()V");

            int literalStart = 0;
            int i = 0;
            while (i < template.length()) {
                char ch = template.charAt(i);
                if (ch != '{') {
                    i++;
                    continue;
                }
                int close = template.indexOf('}', i + 1);
                if (close < 0) {
                    break;
                }
                // find optional array index: {n[m]}
                int bracketOpen = template.indexOf('[', i + 1);
                int argEnd = (bracketOpen >= 0 && bracketOpen < close) ? bracketOpen : close;
                int argIndex = parseArgumentIndex(template, i + 1, argEnd);
                if (argIndex < 0 || argIndex >= argumentTypes.length) {
                    i = close + 1;
                    continue;
                }
                appendLiteral(template.substring(literalStart, i));
                if (bracketOpen >= 0 && bracketOpen < close) {
                    int elemIndex = parseArgumentIndex(template, bracketOpen + 1, close);
                    if (elemIndex >= 0) {
                        appendArrayElement(argumentTypes, argIndex, elemIndex);
                        literalStart = close + 1;
                        i = close + 1;
                        continue;
                    }
                }
                appendArgument(argumentTypes, argIndex);
                literalStart = close + 1;
                i = close + 1;
            }
            appendLiteral(template.substring(literalStart));

            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "toString", "()Ljava/lang/String;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
        }

        private int parseArgumentIndex(String value, int start, int end) {
            if (start >= end) {
                return -1;
            }
            int result = 0;
            for (int i = start; i < end; i++) {
                char ch = value.charAt(i);
                if (ch < '0' || ch > '9') {
                    return -1;
                }
                result = result * 10 + (ch - '0');
            }
            return result;
        }

        private void appendLiteral(String literal) {
            if (literal.length() == 0) {
                return;
            }
            mv.visitLdcInsn(literal);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "append", "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
        }

        private void appendArgument(Type[] argumentTypes, int argumentIndex) {
            Type type = argumentTypes[argumentIndex];
            mv.visitVarInsn(type.getOpcode(ILOAD), getArgumentLocalIndex(argumentTypes, argumentIndex));
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "append", getAppendDescriptor(type));
        }

        private void appendArrayElement(Type[] argumentTypes, int argumentIndex, int elementIndex) {
            // push array ref, push index, load element
            mv.visitVarInsn(ALOAD, getArgumentLocalIndex(argumentTypes, argumentIndex));
            mv.visitLdcInsn(elementIndex);
            mv.visitInsn(IALOAD);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "append", "(F)Ljava/lang/StringBuffer;");
        }

        private int getArgumentLocalIndex(Type[] argumentTypes, int argumentIndex) {
            int localIndex = isStatic() ? 0 : 1;
            for (int i = 0; i < argumentIndex; i++) {
                localIndex += argumentTypes[i].getSize();
            }
            return localIndex;
        }

        private boolean isStatic() {
            return (access & ACC_STATIC) != 0;
        }

        private String getAppendDescriptor(Type type) {
            switch (type.getSort()) {
                case Type.BOOLEAN:
                    return "(Z)Ljava/lang/StringBuffer;";
                case Type.CHAR:
                    return "(C)Ljava/lang/StringBuffer;";
                case Type.BYTE:
                case Type.SHORT:
                case Type.INT:
                    return "(I)Ljava/lang/StringBuffer;";
                case Type.FLOAT:
                    return "(F)Ljava/lang/StringBuffer;";
                case Type.LONG:
                    return "(J)Ljava/lang/StringBuffer;";
                case Type.DOUBLE:
                    return "(D)Ljava/lang/StringBuffer;";
                default:
                    return "(Ljava/lang/Object;)Ljava/lang/StringBuffer;";
            }
        }
    }
}
