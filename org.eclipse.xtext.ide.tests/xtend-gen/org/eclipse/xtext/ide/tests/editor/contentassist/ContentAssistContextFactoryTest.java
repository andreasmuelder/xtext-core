/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtext.ide.tests.editor.contentassist;

import com.google.inject.Inject;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.ParserRule;
import org.eclipse.xtext.ide.editor.contentassist.antlr.ContentAssistContextFactory;
import org.eclipse.xtext.ide.tests.editor.contentassist.ContentAssistContextTestHelper;
import org.eclipse.xtext.ide.tests.testlanguage.TestLanguageIdeInjectorProvider;
import org.eclipse.xtext.ide.tests.testlanguage.services.TestLanguageGrammarAccess;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.XtextRunner;
import org.eclipse.xtext.xbase.lib.Extension;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Moritz Eysholdt - Initial contribution and API
 */
@RunWith(XtextRunner.class)
@InjectWith(TestLanguageIdeInjectorProvider.class)
@SuppressWarnings("all")
public class ContentAssistContextFactoryTest {
  @Inject
  @Extension
  private ContentAssistContextTestHelper _contentAssistContextTestHelper;
  
  @Inject
  private ContentAssistContextFactory factory;
  
  @Inject
  private TestLanguageGrammarAccess grammar;
  
  @Test
  public void testSimple1() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("type Foo <|>{");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("int bar");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    this._contentAssistContextTestHelper.setDocument(_builder.toString());
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("context0 {");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("Keyword: TypeDeclaration:\'extends\'");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("Keyword: TypeDeclaration:\'{\'");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    String _string = _builder_1.toString();
    String _firstSetGrammarElementsToString = this._contentAssistContextTestHelper.firstSetGrammarElementsToString(this.factory);
    Assert.assertEquals(_string, _firstSetGrammarElementsToString);
  }
  
  @Test
  public void testSimple2() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("type Foo {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("<|>int bar");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    this._contentAssistContextTestHelper.setDocument(_builder.toString());
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("context0 {");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("Assignment: TypeDeclaration:properties+= *");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("RuleCall: TypeDeclaration:properties+=Property");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("Assignment: Property:type= ");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("RuleCall: Property:type=Type");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("RuleCall: Type:TypeReference");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("Assignment: TypeReference:typeRef= ");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("RuleCall: Type:PrimitiveType");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("Assignment: PrimitiveType:name= ");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("Keyword: PrimitiveType:name=\'string\'");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("Keyword: PrimitiveType:name=\'int\'");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("Keyword: PrimitiveType:name=\'boolean\'");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("Keyword: TypeDeclaration:\'}\'");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    String _string = _builder_1.toString();
    String _firstSetGrammarElementsToString = this._contentAssistContextTestHelper.firstSetGrammarElementsToString(this.factory);
    Assert.assertEquals(_string, _firstSetGrammarElementsToString);
  }
  
  @Test
  public void testBeginning() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("<|>type Foo {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("int bar");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    this._contentAssistContextTestHelper.setDocument(_builder.toString());
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("context0 {");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("Assignment: Model:types+= *");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("RuleCall: Model:types+=TypeDeclaration");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("Keyword: TypeDeclaration:\'type\'");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("RuleCall: <AbstractElement not contained in AbstractRule!>:Model");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    String _string = _builder_1.toString();
    String _firstSetGrammarElementsToString = this._contentAssistContextTestHelper.firstSetGrammarElementsToString(this.factory);
    Assert.assertEquals(_string, _firstSetGrammarElementsToString);
  }
  
  @Test
  public void testCustomEntryPoint() {
    ParserRule _propertyRule = this.grammar.getPropertyRule();
    this._contentAssistContextTestHelper.setEntryPoint(_propertyRule);
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("int <|>bar");
    _builder.newLine();
    this._contentAssistContextTestHelper.setDocument(_builder.toString());
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("context0 {");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("Assignment: Type:arrayDiemensions+= ");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("Keyword: Type:arrayDiemensions+=\'[\'");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("Assignment: Property:name= ");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("RuleCall: Property:name=ID");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    String _string = _builder_1.toString();
    String _firstSetGrammarElementsToString = this._contentAssistContextTestHelper.firstSetGrammarElementsToString(this.factory);
    Assert.assertEquals(_string, _firstSetGrammarElementsToString);
  }
  
  @Test
  public void testCustomEntryPointBeginning() {
    ParserRule _propertyRule = this.grammar.getPropertyRule();
    this._contentAssistContextTestHelper.setEntryPoint(_propertyRule);
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("<|>int bar");
    _builder.newLine();
    this._contentAssistContextTestHelper.setDocument(_builder.toString());
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("context0 {");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("Assignment: Property:type= ");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("RuleCall: Property:type=Type");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("RuleCall: Type:TypeReference");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("Assignment: TypeReference:typeRef= ");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("RuleCall: Type:PrimitiveType");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("Assignment: PrimitiveType:name= ");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("Keyword: PrimitiveType:name=\'string\'");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("Keyword: PrimitiveType:name=\'int\'");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("Keyword: PrimitiveType:name=\'boolean\'");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("RuleCall: <AbstractElement not contained in AbstractRule!>:Type");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    String _string = _builder_1.toString();
    String _firstSetGrammarElementsToString = this._contentAssistContextTestHelper.firstSetGrammarElementsToString(this.factory);
    Assert.assertEquals(_string, _firstSetGrammarElementsToString);
  }
}
