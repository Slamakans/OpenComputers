package li.cil.oc.server.component

import li.cil.oc.api.Network
import li.cil.oc.api.driver.EnvironmentHost
import li.cil.oc.api.machine.{Arguments, Callback, Context}
import li.cil.oc.api.network._
import li.cil.oc.api.tileentity.Rotatable
import li.cil.oc.common.component
import net.minecraft.tileentity.TileEntitySign

class UpgradeSign(val host: EnvironmentHost with Rotatable) extends component.ManagedComponent {
  val node = Network.newNode(this, Visibility.Network).
    withComponent("sign", Visibility.Neighbors).
    withConnector().
    create()

  // ----------------------------------------------------------------------- //

  @Callback(doc = """function():string -- Get the text on the sign in front of the robot.""")
  def getValue(context: Context, args: Arguments): Array[AnyRef] = {
    findSign match {
      case Some(sign) => result(sign.signText.mkString("\n"))
      case _ => result(Unit, "no sign")
    }
  }

  @Callback(doc = """function(value:string):string -- Set the text on the sign in front of the robot.""")
  def setValue(context: Context, args: Arguments): Array[AnyRef] = {
    val text = args.checkString(0).lines.padTo(4, "").map(line => if (line.length > 15) line.substring(0, 15) else line)
    findSign match {
      case Some(sign) =>
        text.copyToArray(sign.signText)
        host.world.markBlockForUpdate(sign.xCoord, sign.yCoord, sign.zCoord)
        result(sign.signText.mkString("\n"))
      case _ => result(Unit, "no sign")
    }
  }

  private def findSign = {
    val (x, y, z) = (math.floor(host.xPosition).toInt, math.floor(host.yPosition).toInt, math.floor(host.zPosition).toInt)
    host.world.getTileEntity(x, y, z) match {
      case sign: TileEntitySign => Option(sign)
      case _ => host.world.getTileEntity(x + host.facing.offsetX, y + host.facing.offsetY, z + host.facing.offsetZ) match {
        case sign: TileEntitySign => Option(sign)
        case _ => None
      }
    }
  }
}
