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

package uk.co.samuelwall.materialtaptargetprompt.extras.focals;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import android.view.View;

import uk.co.samuelwall.materialtaptargetprompt.extras.PromptFocal;
import uk.co.samuelwall.materialtaptargetprompt.extras.PromptOptions;
import uk.co.samuelwall.materialtaptargetprompt.extras.PromptUtils;

/**
 * Prompt focal implementation to draw the focal as a circle.
 */
public class CirclePromptFocal extends PromptFocal
{
    Paint mPaint;
    int mRippleAlpha;
    float mRadius;
    float mBaseRadius;
    float mRippleRadius;
    int mBaseAlpha;
    PointF mPosition;
    RectF mBounds;
    Path mPath;

    /**
     * Constructor.
     */
    public CirclePromptFocal()
    {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPosition = new PointF();
        mBounds = new RectF();
    }

    /**
     * Set the focal radius.
     *
     * @param radius The radius for the circle to be drawn.
     * @return This prompt focal
     */
    @NonNull
    public CirclePromptFocal setRadius(final float radius)
    {
        mBaseRadius = radius;
        return this;
    }

    @NonNull
    @Override
    public RectF getBounds()
    {
        return mBounds;
    }

    @NonNull
    @Override
    public Path getPath()
    {
        return mPath;
    }

    @Override
    public void setColour(@ColorInt int colour)
    {
        mPaint.setColor(colour);
        mBaseAlpha = Color.alpha(colour);
        mPaint.setAlpha(mBaseAlpha);
    }

    @Override
    public void prepare(@NonNull PromptOptions options, @NonNull View target, final int[] promptViewPosition)
    {
        final int[] targetPosition = new int[2];
        target.getLocationInWindow(targetPosition);

        prepare(options, targetPosition[0] - promptViewPosition[0] + (target.getWidth() / 2),
                targetPosition[1] - promptViewPosition[1] + (target.getHeight() / 2));
    }

    @Override
    public void prepare(@NonNull PromptOptions options, float targetX, float targetY)
    {
        mPosition.x = targetX;
        mPosition.y = targetY;
        mBounds.left = targetX - mBaseRadius;
        mBounds.top = targetY - mBaseRadius;
        mBounds.right = targetX + mBaseRadius;
        mBounds.bottom = targetY + mBaseRadius;
    }

    @Override
    public void update(@NonNull PromptOptions options, float revealModifier,
                       float alphaModifier)
    {
        mPaint.setAlpha((int) (mBaseAlpha * alphaModifier));
        mRadius = mBaseRadius * revealModifier;

        mPath = new Path();
        mPath.addCircle(mPosition.x, mPosition.y, mRadius, Path.Direction.CW);
    }

    @Override
    public void updateRipple(float revealModifier, float alphaModifier)
    {
        mRippleRadius = mBaseRadius * revealModifier;
        mRippleAlpha = (int) (mBaseRippleAlpha * alphaModifier);
    }

    @Override
    public void draw(@NonNull Canvas canvas)
    {
        //Draw the ripple
        if (mDrawRipple)
        {
            final int oldAlpha = mPaint.getAlpha();
            final int oldColor = mPaint.getColor();
            if (oldColor == Color.TRANSPARENT)
            {
                mPaint.setColor(Color.WHITE);
            }
            mPaint.setAlpha(mRippleAlpha);
            canvas.drawCircle(mPosition.x, mPosition.y, mRippleRadius, mPaint);
            mPaint.setColor(oldColor);
            mPaint.setAlpha(oldAlpha);
        }

        // canvas.drawRect(mBounds, mPaint);

        canvas.drawPath(getPath(), mPaint);
    }

    @Override
    public boolean contains(float x, float y)
    {
        return PromptUtils.isPointInCircle(x, y, mPosition, mRadius);
    }
}
