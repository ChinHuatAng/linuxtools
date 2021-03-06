/*******************************************************************************
 * Copyright (c) 2015-2016 Open Analytics NV and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.editor.scanner;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.linuxtools.internal.docker.editor.syntax.SyntaxColors;

public class DockerInstructionScanner extends RuleBasedScanner {

	public DockerInstructionScanner() {
		IToken instructionToken = new Token(new TextAttribute(SyntaxColors.getInstructionColor()));
		IToken stringToken = new Token(new TextAttribute(SyntaxColors.getStringColor()));
		IToken varToken = new Token(new TextAttribute(SyntaxColors.getVariableColor()));
		IToken commentToken = new Token(new TextAttribute(SyntaxColors.getCommentColor()));

		IRule[] rules = new IRule[5];
		rules[0] = new InstructionWordRule(instructionToken);
		rules[1] = new SingleLineRule("\"", "\"", stringToken, '\\');
		rules[2] = new SingleLineRule("\'", "\'", stringToken, '\\');
		rules[3] = new SingleLineRule("${", "}", varToken, '\\');
		rules[4] = new EndOfLineRule("#", commentToken) {
			@Override
			public IToken evaluate(ICharacterScanner scanner, boolean resume) {
				if (getColumn() > 0)
					return Token.UNDEFINED;
				return super.evaluate(scanner, resume);
			}
		};

		setRules(rules);
	}
}
