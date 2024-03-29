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

package uk.co.samuelwall.materialtaptargetprompt.extras;

import android.graphics.Path;
import android.graphics.RectF;
import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import android.view.View;

/**
 * Used to render the prompt focal.
 */
public abstract class PromptFocal implements PromptUIElement
{
    /**
     * Should the ripple be drawn.
     */
    protected boolean mDrawRipple;

    /**
     * The alpha value for the ripple.
     */
    protected int mBaseRippleAlpha;

    /**
     * Sets whether the ripple is drawn around the focal.
     *
     * @param drawRipple True to draw the ripple.
     */
    public void setDrawRipple(final boolean drawRipple)
    {
        mDrawRipple = drawRipple;
    }

    /**
     * Sets the alpha value to use for the ripple colour.
     *
     * @param rippleAlpha The ripple alpha value between 0 - 255
     */
    public void setRippleAlpha(final @IntRange(from = 0, to = 255) int rippleAlpha)
    {
        mBaseRippleAlpha = rippleAlpha;
    }

    /**
     * Get the focal path to be drawn. Override this to support a transparent focal colour.
     * @return The path used to draw the focal
     */
    public Path getPath() {
        return null;
    }

    /**
     * Set the focal colour.
     *
     * @param colour Int colour.
     */
    public abstract void setColour(@ColorInt int colour);

    /**
     * Get the focal bounds at scale 1.
     * @return The screen area that the focal is drawn in at scale 1.
     */
    @NonNull
    public abstract RectF getBounds();

    /**
     * Setup the focal ready for rendering when targeting a view, called prior to first render.
     *
     * @param options The option that the prompt was built from.
     * @param target The prompt target view.
     * @param promptViewPosition The prompt views screen position.
     */
    public abstract void prepare(@NonNull final PromptOptions options,
                                 @NonNull View target, final int[] promptViewPosition);

    /**
     * Setup the focal ready for rendering when targeting a point on the screen, called prior to first render.
     *
     * @param options The option that the prompt was built from.
     * @param targetX The target screen x position.
     * @param targetY The target screen y position.
     */
    public abstract void prepare(@NonNull final PromptOptions options, float targetX, float targetY);

    /**
     * Update the ripple around the focal.
     *
     * @param revealModifier The amount to scale the ripple by where a 1 value is the same size as the focal.
     * @param alphaModifier The amount to modify the ripple alpha by.
     */
    public abstract void updateRipple(@FloatRange(from = 0, to = 2) float revealModifier,
                                      @FloatRange(from = 0, to = 1) float alphaModifier);
}
