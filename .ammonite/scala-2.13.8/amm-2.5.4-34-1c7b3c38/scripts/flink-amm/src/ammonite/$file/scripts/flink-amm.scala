
package ammonite
package $file.scripts
import _root_.ammonite.interp.api.InterpBridge.{
  value => interp
}
import _root_.ammonite.interp.api.InterpBridge.value.{
  exit,
  scalaVersion
}
import _root_.ammonite.interp.api.IvyConstructor.{
  ArtifactIdExt,
  GroupIdExt
}
import _root_.ammonite.compiler.CompilerExtensions.{
  CompilerInterpAPIExtensions,
  CompilerReplAPIExtensions
}
import _root_.ammonite.runtime.tools.{
  browse,
  grep,
  time,
  tail
}
import _root_.ammonite.compiler.tools.{
  desugar,
  source
}
import _root_.mainargs.{
  arg,
  main
}
import _root_.ammonite.repl.tools.Util.{
  PathRead
}
import _root_.ammonite.repl.ReplBridge.value.{
  codeColorsImplicit
}


object `flink-amm`{
/*<script>*/import $ivy.$                                   
import $ivy.$                                      
import $ivy.$                                                    // It contains one Java Factory class for Scala Products. Scala code is not required

import io.findify.flink.api._
import io.findify.flinkadt.api._

val env = StreamExecutionEnvironment.getExecutionEnvironment
/*</script>*/ /*<generated>*/
def $main() = { scala.Iterator[String]() }
  override def toString = "flink$minusamm"
  /*</generated>*/
}
