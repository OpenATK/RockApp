package com.openatk.rockapp.libcommon;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;

/*
 * A view which extends over its parent views when "opened". It is a sliding menu view.
 */
public class SlideLayout extends LinearLayout {
	private int speed;
	private SlideOutDirection slideDirection;
	private boolean isOpen;
	private AlphaAnimation fadeIn;
	private AlphaAnimation fadeOut;

	/*
	 * Enumeration use to translate XML attribute to the java
	 */
	private enum SlideOutDirection {
		UP, DOWN, RIGHT, LEFT
	};
	
	/*
	 * Enumeration to describe the slide type
	 */
	private enum SlideType {
		SHOW, HIDE
	};

	/*
	 * Called by android when inflating the view. Configures the view and
	 * animations.
	 */
	public SlideLayout(final Context context, AttributeSet attrs) {
		super(context, attrs);

		// Get, interpret, attributes, and store them
		speed = 150;
		switch (1) {
			case 1:
			default:
				slideDirection = SlideOutDirection.UP;
			break;

			case 2:
				slideDirection = SlideOutDirection.DOWN;
			break;

			case 3:
				slideDirection = SlideOutDirection.RIGHT;
			break;

			case 4:
				slideDirection = SlideOutDirection.LEFT;
			break;
		}

		// Start closed
		isOpen = false;

		// cache the fading animations as they don't change
		fadeIn = new AlphaAnimation(0.0f, 1.0f);
		fadeOut = new AlphaAnimation(1.0f, 0.f);
	}

	/*
	 * Returns if the view is currently open (showing) or not
	 */
	public boolean isOpen() {
		return this.isOpen;
	}

	/*
	 * Returns the translate animation for the requested slideOutDirection and direction
	 */
	private TranslateAnimation calcTranslateAnimation(SlideType slideType) {
		TranslateAnimation anim = null;
		
		// Pick the right animation end points
		switch (slideDirection) {
			case UP:
				if(slideType == SlideType.SHOW) {
					anim = new TranslateAnimation(0.0f, 0.0f, getHeight(), 0.0f); 
				} else {
					anim = new TranslateAnimation(0.0f, 0.0f, 0.0f, getHeight()); 
				}
			break;

			case DOWN:
				if(slideType == SlideType.SHOW) {
					anim = new TranslateAnimation(0.0f, 0.0f, -getHeight(), 0.0f); 
				} else {
					anim = new TranslateAnimation(0.0f, 0.0f, 0.0f, -getHeight()); 
				}
			break;

			case RIGHT:
				if(slideType == SlideType.SHOW) {
					anim = new TranslateAnimation(-getWidth(), 0.0f, 0.0f, 0.0f);
				} else {
					anim = new TranslateAnimation(0.0f, -getWidth(), 0.0f, 0.0f);
				}
			break;

			case LEFT:
				if(slideType == SlideType.SHOW) {
					anim = new TranslateAnimation(getWidth(), 0.0f, 0.0f, 0.0f);
				} else {
					anim = new TranslateAnimation(0.0f, getWidth(), 0.0f, 0.0f);
				}
			break;
		}
		
		// Pick the right direction to play the animation
		
		return anim;
	}

	/*
	 * Take the menu from the hidden state to the displaying state
	 */
	public void show() {
		if (isOpen)
			return;
		
		// Set early so that rapidly calling show() doesn't show multiple animations
		isOpen = true;

		AnimationSet set = new AnimationSet(true);
		set.addAnimation(calcTranslateAnimation(SlideType.SHOW));
		set.addAnimation(fadeIn);
		set.setDuration(speed);
		set.setInterpolator(new AccelerateInterpolator(1.0f));
		set.setAnimationListener(new ShowAnimationListener());
		
		startAnimation(set);
	}

	/*
	 * Take the menu from the displaying date to the hidden state
	 */
	public void hide() {
		if (!isOpen)
			return;

		// Set early so that rapidly calling hide() doesn't show multiple
		// animations
		isOpen = false;

		AnimationSet set = new AnimationSet(true);
		set.addAnimation(calcTranslateAnimation(SlideType.HIDE));
		set.addAnimation(fadeOut);
		set.setDuration(speed);
		set.setInterpolator(new AccelerateInterpolator(1.0f));
		set.setAnimationListener(new HideAnimationListener());

		startAnimation(set);
	}

	/*
	 * AnimationListener which is used to respond to animation events caused
	 * from calling show()
	 */
	private class ShowAnimationListener implements Animation.AnimationListener {
		public void onAnimationEnd(Animation animation) {
			// not needed
		}

		public void onAnimationRepeat(Animation animation) {
			// not needed
		}

		public void onAnimationStart(Animation animation) {
			// Make view being animated visible
			setVisibility(View.VISIBLE);
		}
	}

	/*
	 * AnimationListener which is used to respond to animation events caused
	 * from calling hide()
	 */
	private class HideAnimationListener implements Animation.AnimationListener {
		public void onAnimationEnd(Animation animation) {
			// Make view being animated no longer visible
			setVisibility(View.GONE);
		}

		public void onAnimationRepeat(Animation animation) {
			// not needed
		}

		public void onAnimationStart(Animation animation) {
			// not needed
		}
	}
}
