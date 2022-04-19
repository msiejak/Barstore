// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.android.ads.nativetemplates

import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable

/** A class containing the optional styling options for the Native Template. *  */
class NativeTemplateStyle {
    // Call to action typeface.
    var callToActionTextTypeface: Typeface? = null
        private set

    // Size of call to action text.
    var callToActionTextSize = 0f
        private set

    // Call to action typeface color in the form 0xAARRGGBB.
    var callToActionTypefaceColor = 0
        private set

    // Call to action background color.
    var callToActionBackgroundColor: ColorDrawable? = null
        private set

    // All templates have a primary text area which is populated by the native ad's headline.
    // Primary text typeface.
    var primaryTextTypeface: Typeface? = null
        private set

    // Size of primary text.
    var primaryTextSize = 0f
        private set

    // Primary text typeface color in the form 0xAARRGGBB.
    var primaryTextTypefaceColor = 0
        private set

    // Primary text background color.
    var primaryTextBackgroundColor: ColorDrawable? = null
        private set

    // The typeface, typeface color, and background color for the second row of text in the template.
    // All templates have a secondary text area which is populated either by the body of the ad or
    // by the rating of the app.
    // Secondary text typeface.
    var secondaryTextTypeface: Typeface? = null
        private set

    // Size of secondary text.
    var secondaryTextSize = 0f
        private set

    // Secondary text typeface color in the form 0xAARRGGBB.
    var secondaryTextTypefaceColor = 0
        private set

    // Secondary text background color.
    var secondaryTextBackgroundColor: ColorDrawable? = null
        private set

    // The typeface, typeface color, and background color for the third row of text in the template.
    // The third row is used to display store name or the default tertiary text.
    // Tertiary text typeface.
    var tertiaryTextTypeface: Typeface? = null
        private set

    // Size of tertiary text.
    var tertiaryTextSize = 0f
        private set

    // Tertiary text typeface color in the form 0xAARRGGBB.
    var tertiaryTextTypefaceColor = 0
        private set

    // Tertiary text background color.
    var tertiaryTextBackgroundColor: ColorDrawable? = null
        private set

    // The background color for the bulk of the ad.
    var mainBackgroundColor: ColorDrawable? = null
        private set

    /** A class that provides helper methods to build a style object. *  */
    class Builder {
        private val styles: NativeTemplateStyle = NativeTemplateStyle()
        fun withMainBackgroundColor(mainBackgroundColor: ColorDrawable?): Builder {
            styles.mainBackgroundColor = mainBackgroundColor
            return this
        }

        fun build(): NativeTemplateStyle {
            return styles
        }

    }
}