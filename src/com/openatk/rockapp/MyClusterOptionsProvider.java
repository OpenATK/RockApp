package com.openatk.rockapp;

/*
 * Copyright (C) 2013 Maciej G—rski
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
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.support.v4.util.LruCache;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.List;

import pl.mg6.android.maps.extensions.ClusterOptions;
import pl.mg6.android.maps.extensions.ClusterOptionsProvider;
import pl.mg6.android.maps.extensions.Marker;

public class MyClusterOptionsProvider implements ClusterOptionsProvider {

	private static final int[] res = { R.drawable.rock_group, R.drawable.rock_group, R.drawable.rock_group, R.drawable.rock_group, R.drawable.rock_group };

	private static final int[] forCounts = { 5, 10, 20, 30, Integer.MAX_VALUE };

    private Bitmap[] baseBitmaps;
    private LruCache<Integer, BitmapDescriptor> cache = new LruCache<Integer, BitmapDescriptor>(128);

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Rect bounds = new Rect();

    private ClusterOptions clusterOptions = new ClusterOptions();

    public MyClusterOptionsProvider(Resources resources) {
        baseBitmaps = new Bitmap[res.length];
        for (int i = 0; i < res.length; i++) {
            baseBitmaps[i] = BitmapFactory.decodeResource(resources, res[i]);
        }
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Align.CENTER);
        paint.setTextSize(resources.getDimension(R.dimen.text_size));
    }

	@Override
	public ClusterOptions getClusterOptions(List<Marker> markers) {
		int markersCount = markers.size();
		BitmapDescriptor cachedIcon = cache.get(markersCount);
		if (cachedIcon != null) {
		    return clusterOptions.icon(cachedIcon);
		}
		
		Bitmap base;
		int i = 0;
		do {
		    base = baseBitmaps[i];
		} while (markersCount >= forCounts[i++]);
		
		Bitmap bitmap = base.copy(Config.ARGB_8888, true);
		
		String text = String.valueOf(markersCount);
		paint.getTextBounds(text, 0, text.length(), bounds);
		//float x = bitmap.getWidth() / 2.0f;
		//float y = (bitmap.getHeight() - bounds.height()) / 2.0f - bounds.top;
		float x = bitmap.getWidth() - (bitmap.getWidth() * 0.12f) - (bounds.width() / 2.0f);
		float y = -1.0f * bounds.top + (bitmap.getHeight() * 0.03f);
		
		Canvas canvas = new Canvas(bitmap);
		canvas.drawText(text, x, y, paint);
		
		BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(bitmap);
		cache.put(markersCount, icon);
		
		return clusterOptions.icon(icon);
	}
}
