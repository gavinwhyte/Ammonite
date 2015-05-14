package ammonite.repl.frontend

import scalaparse._
import acyclic.file
import language.implicitConversions
import syntax._
import fastparse._
/**
 * Parser for Scala syntax.
 */
object Highlighter extends Core with Types with Exprs with Xml{
  def highlight(s: String) = {
    jumps.clear()
    this.BlockStat.rep(Semis).parse(s)
  }
  val jumps = collection.mutable.Map.empty[Int, String]

  private implicit def wspStr(s: String) = P(WL ~ s)(Utils.literalize(s).toString)


  val TmplBody: P0 = {
    val Prelude = P( (Annot ~ OneNLMax).rep ~ (Mod ~! Pass).rep )
    val TmplStat = P( Import | Prelude ~ BlockDef | StatCtx.Expr )
    val SelfType = P( (`this` | Id | `_`) ~ (`:` ~ InfixType).? ~ `=>` )
    P( "{" ~! SelfType.? ~ Semis.? ~ TmplStat.rep(Semis) ~ `}` )
  }

  val ValVarDef = P( BindPattern.rep1("," ~! Pass) ~ (`:` ~! Type).? ~ (`=` ~! StatCtx.Expr).? )

  val FunDef = {
    val Body = P( `=` ~! `macro`.? ~ StatCtx.Expr | OneNLMax ~ "{" ~ Block ~ "}" )
    P( FunSig ~ (`:` ~! Type).? ~ Body.? )
  }

  val BlockDef: P0 = P( Dcl | TraitDef | ClsDef | ObjDef )

  val ClsDef = {
    val ClsAnnot = P( `@` ~ SimpleType ~ ArgList )
    val Prelude = P( NotNewline ~ ( ClsAnnot.rep1 ~ AccessMod.? | ClsAnnot.rep ~ AccessMod) )
    val ClsArgMod = P( (Mod.rep ~ (`val` | `var`)) )
    val ClsArg = P( Annot.rep ~ ClsArgMod.? ~ Id ~ `:` ~ Type ~ (`=` ~ ExprCtx.Expr).? )

    val Implicit = P( OneNLMax ~ "(" ~ `implicit` ~ ClsArg.rep1(",") ~ ")" )
    val ClsArgs = P( OneNLMax ~ "(" ~ ClsArg.rep(",") ~ ")" )
    val AllArgs = P( ClsArgs.rep1 ~ Implicit.? | Implicit )
    P( `case`.? ~ `class` ~! Id ~ TypeArgList.? ~ Prelude.? ~ AllArgs.? ~ DefTmpl.? )
  }

  val Constrs = P( Constr.rep1(`with` ~! Pass) )
  val EarlyDefTmpl = P( TmplBody ~ (`with` ~! Constr).rep ~ TmplBody.? )
  val NamedTmpl = P( Constrs ~ TmplBody.? )

  val DefTmpl = P( (`extends` | `<:`) ~ AnonTmpl | TmplBody)
  val AnonTmpl = P( EarlyDefTmpl | NamedTmpl | TmplBody )

  val TraitDef = P( `trait` ~! Id ~ TypeArgList.? ~ DefTmpl.? )

  val ObjDef: P0 = P( `case`.? ~ `object` ~! Id ~ DefTmpl.? )

  val Constr = P( AnnotType ~ (NotNewline ~ ParenArgList ).rep )

  val PkgObj = P( ObjDef )
  val PkgBlock = P( QualId ~! `{` ~ TopStatSeq.? ~ `}` )
  val TopStatSeq: P0 = {
    val Tmpl = P( (Annot ~ OneNLMax).rep ~ Mod.rep ~ (TraitDef | ClsDef | ObjDef) )
    val TopStat = P( `package` ~! (PkgBlock | PkgObj) | Import | Tmpl )
    P( TopStat.rep1(Semis) )
  }
  val TopPkgSeq = P( (`package` ~ QualId ~ !(WS ~ "{")).rep1(Semis) )
  val CompilationUnit: P0 = {
    val Body = P( TopPkgSeq ~ (Semis ~ TopStatSeq).? | TopStatSeq )
    P( Semis.? ~ Body.? ~ Semis.? ~ WL ~ End)
  }
}