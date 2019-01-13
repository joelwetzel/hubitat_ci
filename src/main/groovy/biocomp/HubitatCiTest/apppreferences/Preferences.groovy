package biocomp.hubitatCiTest.apppreferences

import biocomp.hubitatCiTest.util.NamedParametersValidator
import biocomp.hubitatCiTest.util.Utility
import groovy.transform.TypeChecked

@TypeChecked
class Preferences implements
        biocomp.hubitatCiTest.emulation.appApi.Preferences
{
    Preferences(Script parentScript, Map options) {
        this.parentScript = parentScript
    }

    final Script parentScript

    /**
     * Pages for multiple-page apps.*/
    final List<Page> pages = []

    /**
     * Dynamic pages for multiple-page apps.*/
    final List<Page> dynamicPages = []


    /**
     * Methods to be called to generate dynamic pages.
     * Called via registerDynamicPages()*/
    final List<Closure> dynamicRegistrations = []

    /**
     * Used when preferences().section() is used directly.
     * Not compatible with multiple pages.*/
    Page specialSinglePage = null

    Page getSpecialSinglePage() {
        assert pages.size() == 0: "You can't use single-page section() calls when you already added pages: ${pages}"

        if (this.@specialSinglePage == null) {
            this.@specialSinglePage = Page.makeSinglePage()
            pages << this.@specialSinglePage
        }

        return this.@specialSinglePage
    }

    /**
     * Run validations after all pages and sections were added*/
    void validate(EnumSet<ValidationFlags> flags, boolean okIfRegisteredDynamicPages = true) {
        if (this.@specialSinglePage != null) {
            this.@specialSinglePage.validate(flags)
        }

        if (!flags.contains(ValidationFlags.DontValidatePreferences)) {
            // Needs to have pages
            if (!okIfRegisteredDynamicPages || dynamicRegistrations.size() == 0) {
                assert (pages.size() != 0 || dynamicPages.size() != 0): "preferences() has to have pages (got ${pages.size()}), dynamic pages (got ${dynamicPages.size()})"
            }

            // Page names must be unique
            def allNames = pages.collect { it.readName() } + dynamicPages.collect { it.readName() }
            def uniqueNames = [] as Set
            allNames.each {
                assert !uniqueNames.contains(
                        it): "Page '${it}' is not a unique page name. Page names must be unique. Please fix that. All page names: ${allNames}"
                uniqueNames << it
            }

            // Multi-page app needs explicit 'install' (otherwise you won't be able to install this)
            if (this.@specialSinglePage == null && !flags.contains(ValidationFlags.AllowMissingInstall)) {
                assert pages.findAll { it.options.install == true } + dynamicPages.findAll {
                    it.options.install == true
                }: "There is no 'install: true' option specified on any page (and it's not a special one-page app). This means you can't install the app. Please add the button to a page."
            }
        }
    }

    void validateSingleDynamicPageFor(EnumSet<ValidationFlags> flags, String methodName) {
        if (!flags.contains(ValidationFlags.DontValidatePreferences)) {
            def set = [] as Set<String>;
            dynamicPages.each {
                assert it.generationMethodName: "Page ${it.readName()} was not produed by a separate method. This is not supported."
                assert !set.contains(
                        it.generationMethodName): "Method ${methodName} contains more than one dynamicPage() invocation: ${dynamicPages.findAll { it.generationMethodName == methodName }.collect { it.readName() }}"
                set << it.generationMethodName
            }
        }
    }

    /**
     * Calls methods that create dymaic pages that were discovered previously*/
    void registerDynamicPages(EnumSet<ValidationFlags> flags) {
        dynamicRegistrations.each { it() }

        // Now validate and make sure something was actually created, not just methods registered.
        validate(flags, false)
    }
}