package biocomp.hubitatCi.apppreferences


import biocomp.hubitatCi.validation.Flags
import biocomp.hubitatCi.validation.NamedParametersValidator
import biocomp.hubitatCi.validation.AppValidator
import groovy.transform.TupleConstructor
import groovy.transform.TypeChecked

@TupleConstructor
@TypeChecked
class Section {
    int index
    String title
    Map options

    List children = []

    static private final NamedParametersValidator paramValidator = NamedParametersValidator.make{
        boolParameter("hideable", notRequired())
        boolParameter("hidden", notRequired())
        boolParameter("mobileOnly", notRequired())
        boolParameter("hideWhenEmpty", notRequired())
    }

    void validate(AppValidator validator)
    {
        if (!validator.hasFlag(Flags.DontValidatePreferences)) {
            paramValidator.validate(this.toString(), options, validator)
            assert children.size() != 0: "Section ${this} must have at least some content"
        }
    }

    @Override
    String toString()
    {
        return "section #${index}(\"${title}\", ${options})"
    }
}
