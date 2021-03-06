/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.ui.test

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.test.espresso.Espresso
import androidx.test.filters.MediumTest
import androidx.ui.core.Modifier
import androidx.ui.core.setContent
import androidx.compose.foundation.Box
import androidx.ui.test.android.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

@MediumTest
class MultipleActivitiesFirstDrawTest {

    @get:Rule
    val testRule = createAndroidComposeRule<Activity1>()

    @Test
    fun test() {
        Espresso.onIdle()
        // doesn't timeout
    }

    class Activity1 : ComponentActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContent { Box(Modifier) }
            startActivity(Intent(this, Activity2::class.java))
        }
    }

    class Activity2 : ComponentActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContent { Box(Modifier) }
        }
    }
}
