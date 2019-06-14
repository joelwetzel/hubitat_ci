package biocomp.hubitatCi.app.preferences

import biocomp.hubitatCi.app.AppValidator
import biocomp.hubitatCi.validation.Flags
import biocomp.hubitatCi.validation.NamedParametersValidator
import groovy.transform.TypeChecked

@TypeChecked
class Label {
    private static final NamedParametersValidator paramValidator = NamedParametersValidator.make {
        stringParameter("title", required(), mustNotBeEmpty())
        stringParameter("description", notRequired(), mustNotBeEmpty())
        stringParameter("image", notRequired(), mustNotBeEmpty())
        boolParameter("required", notRequired())
    }

    Label(Map options, AppValidator validator) {
        this.options = options

        if (!validator.hasFlag(Flags.DontValidatePreferences)) {
            paramValidator.validate(this.toString(), options, validator)
        }
    }

    final Map options
}