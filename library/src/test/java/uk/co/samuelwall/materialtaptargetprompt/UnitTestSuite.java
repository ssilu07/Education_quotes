/*
 * Copyright (C) 2017 Samuel Wall
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.co.samuelwall.materialtaptargetprompt;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import uk.co.samuelwall.materialtaptargetprompt.extras.AlphaSpanUnitTest;
import uk.co.samuelwall.materialtaptargetprompt.extras.PromptOptionsUnitTest;
import uk.co.samuelwall.materialtaptargetprompt.extras.PromptTextUnitTest;
import uk.co.samuelwall.materialtaptargetprompt.extras.PromptUtilsUnitTest;
import uk.co.samuelwall.materialtaptargetprompt.extras.backgrounds.CirclePromptBackgroundUnitTest;
import uk.co.samuelwall.materialtaptargetprompt.extras.backgrounds.FullscreenPromptBackgroundUnitTest;
import uk.co.samuelwall.materialtaptargetprompt.extras.backgrounds.RectanglePromptBackgroundUnitTest;
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.CirclePromptFocalUnitTest;
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocalUnitTest;

// Runs all unit tests.
@RunWith(Suite.class)
@Suite.SuiteClasses({
    /*AlphaSpanUnitTest.class,
    PromptOptionsUnitTest.class,
    PromptTextUnitTest.class,
    PromptUtilsUnitTest.class,
    CirclePromptBackgroundUnitTest.class,
    RectanglePromptBackgroundUnitTest.class,
    FullscreenPromptBackgroundUnitTest.class,
    CirclePromptFocalUnitTest.class,
    RectanglePromptFocalUnitTest.class,
    BuilderUnitTest.class,
    PromptViewUnitTest.class,
    ActivityResourceFinderUnitTest.class,
    DialogResourceFinderUnitTest.class,
    MaterialTapTargetPromptUnitTest.class*/
})
public class UnitTestSuite {}
