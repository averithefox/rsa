package rsa.commands

import com.github.stivais.commodore.Commodore
import com.ricedotwho.rsa.module.impl.dungeon.croesus.AutoCroesus
import rsa.features.dungeon.croesus.CroesusLoader

@CommandInfo(name = "autocroesus", aliases = ["ac"], description = "Configuring and starting AutoCroesus")
class AutoCroesusCommand : Command() {
  override fun build() = Commodore(name()) {
    literal("go") {
      runs {
        RSM.getModule(AutoCroesus::class.java).start()
      }
    }

    literal("forcego") {
      runs {
        RSM.getModule(AutoCroesus::class.java).start(false)
      }
    }

    literal("alwaysbuy") {
      runs { sbId: String? ->
        val sbId = sbId?.uppercase() ?: run {
          AutoCroesus.modMessage("Always Buy: ${CroesusLoader.alwaysBuy}")
          return@runs
        }

        when {
          sbId.equals("reset", ignoreCase = true) -> {
            CroesusLoader.alwaysBuy.clear()
            AutoCroesus.modMessage("Cleared the always buy list")
          }

          CroesusLoader.alwaysBuy.contains(sbId) -> {
            CroesusLoader.alwaysBuy.remove(sbId)
            CroesusLoader.saveAlwaysBuy()
            AutoCroesus.modMessage("Removed $sbId from always buy")
          }

          else -> {
            CroesusLoader.alwaysBuy.add(sbId)
            CroesusLoader.saveAlwaysBuy()
            if (PriceData.getItemCache().containsKey(sbId)) {
              AutoCroesus.modMessage("Added $sbId to always buy")
            } else {
              AutoCroesus.modMessage("Added $sbId to always buy (This item is not known, please double check!)")
            }
          }
        }
      }

      literal("reset") {
        runs {
          CroesusLoader.alwaysBuy.clear()
          AutoCroesus.modMessage("Cleared the always buy list")
        }
      }
    }

    literal("worthless") {
      runs { sbId: String? ->
        val sbId = sbId?.uppercase() ?: run {
          AutoCroesus.modMessage("Worthless: ${CroesusLoader.worthless}")
          return@runs
        }

        when {
          sbId.equals("reset", ignoreCase = true) -> {
            CroesusLoader.worthless.clear()
            AutoCroesus.modMessage("Cleared the worthless list")
          }

          CroesusLoader.worthless.contains(sbId) -> {
            CroesusLoader.worthless.remove(sbId)
            CroesusLoader.saveWorthless()
            AutoCroesus.modMessage("Removed $sbId from worthless")
          }

          else -> {
            CroesusLoader.worthless.add(sbId)
            CroesusLoader.saveWorthless()
            if (PriceData.getItemCache().containsKey(sbId)) {
              AutoCroesus.modMessage("Added $sbId to worthless")
            } else {
              AutoCroesus.modMessage("Added $sbId to worthless (This item is not known, please double check!)")
            }
          }
        }
      }

      literal("reset") {
        runs {
          CroesusLoader.worthless.clear()
          AutoCroesus.modMessage("Cleared the worthless list")
        }
      }
    }
  }.toLiteralArgumentBuilder()
}
