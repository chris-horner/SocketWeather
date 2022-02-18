package codes.chrishorner.socketweather.test

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.util.Locale

/**
 * Enforce a particular default locale for a test. Resets back to default on completion.
 */
class DefaultLocaleRule(val override: Locale) : TestRule {
  override fun apply(
    base: Statement,
    description: Description
  ): Statement {
    return object : Statement() {
      override fun evaluate() {
        val default = Locale.getDefault()

        try {
          Locale.setDefault(override)
          base.evaluate()
        } finally {
          Locale.setDefault(default)
        }
      }
    }
  }
}
