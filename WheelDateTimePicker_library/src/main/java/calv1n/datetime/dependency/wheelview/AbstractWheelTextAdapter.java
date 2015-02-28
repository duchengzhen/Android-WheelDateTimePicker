/*
 *  Copyright 2011 Yuri Kanivets
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package calv1n.datetime.dependency.wheelview;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * Abstract wheel adapter provides common functionality for adapters.
 */
public abstract class AbstractWheelTextAdapter extends AbstractWheelAdapter {

	/**
	 * Text view resource. Used as a default view for adapter.
	 */
	public static final int DEFAULT_ITEM_ID = R.layout.wheel_item;

	public static final int DEFAULT_ITEM_TEXT_ID = R.id.tv_wheel_item;

	/**
	 * Default text color
	 */
	public static final int LABEL_COLOR = 0xFF700070;

	/**
	 * 默认的typeface
	 */
	public static final Typeface DEFAULT_TEXT_TYPEFACE = Typeface.DEFAULT;

	//设置字体
	private int textColor;
	private int textSize;
	private Typeface typeface;

	// Current mContext
	protected Context context;
	// Layout inflater
	protected LayoutInflater inflater;

	// Items resources
	protected int itemResourceId = DEFAULT_ITEM_ID;
	protected int itemTextResourceId = DEFAULT_ITEM_TEXT_ID;

	// Empty items resources
	protected int emptyItemResourceId;

	/**
	 * Constructor
	 *
	 * @param context the current mContext
	 */
	protected AbstractWheelTextAdapter(Context context) {
		this(context, DEFAULT_ITEM_ID, DEFAULT_ITEM_TEXT_ID);
	}

	/**
	 * Constructor
	 *
	 * @param context          the current mContext
	 * @param itemResource     the resource ID for a layout file containing a TextView to use when instantiating items views
	 * @param itemTextResource the resource ID for a text view in the item layout
	 */
	protected AbstractWheelTextAdapter(Context context, int itemResource, int itemTextResource) {
		this.context = context;
		itemResourceId = itemResource;
		itemTextResourceId = itemTextResource;

		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	/**
	 * Gets text color
	 *
	 * @return the text color
	 */
	public int getTextColor() {
		return textColor;
	}

	/**
	 * Sets text color
	 *
	 * @param textColor the text color to saveConfig
	 */
	public void setTextColor(int textColor) {
		this.textColor = textColor;
	}

	/**
	 * Gets text size
	 *
	 * @return the text size
	 */
	public int getTextSize() {
		return textSize;
	}

	/**
	 * Sets text size
	 *
	 * @param textSize the text size to saveConfig
	 */
	public void setTextSize(int textSize) {
		this.textSize = textSize;
	}

	/**
	 * Gets resource Id for items views
	 *
	 * @return the item resource Id
	 */
	public int getItemResource() {
		return itemResourceId;
	}

	/**
	 * Sets resource Id for items views
	 *
	 * @param itemResourceId the resource Id to saveConfig
	 */
	public void setItemResource(int itemResourceId, int itemTextResourceId) {
		this.itemResourceId = itemResourceId;
		this.itemResourceId = itemResourceId;
	}

	/**
	 * Gets resource Id for text view in item layout
	 *
	 * @return the item text resource Id
	 */
	public int getItemTextResource() {
		return itemTextResourceId;
	}


	/**
	 * Gets resource Id for empty items views
	 *
	 * @return the empty item resource Id
	 */
	public int getEmptyItemResource() {
		return emptyItemResourceId;
	}

	/**
	 * Sets resource Id for empty items views
	 *
	 * @param emptyItemResourceId the empty item resource Id to saveConfig
	 */
	public void setEmptyItemResource(int emptyItemResourceId) {
		this.emptyItemResourceId = emptyItemResourceId;
	}


	public Typeface getTypeface() {
		return typeface;
	}

	public void setTypeface(Typeface typeface) {
		this.typeface = typeface;
	}

	/**
	 * Returns text for specified item
	 *
	 * @param index the item index
	 * @return the text of specified items
	 */

	protected abstract CharSequence getItemText(int index);

	@Override
	public View getItem(int index, View convertView, ViewGroup parent) {
		if (index >= 0 && index < getItemsCount()) {
			if (convertView == null) {
				convertView = getView(itemResourceId, parent);
			}
			TextView textView = getTextView(convertView, itemTextResourceId);
			if (textView != null) {
				CharSequence text = getItemText(index);
				if (text == null) {
					text = "";
				}
				configureTextView(textView);
				textView.setText(text);
			}
			return convertView;
		}
		return null;
	}

	@Override
	public View getEmptyItem(View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = getView(emptyItemResourceId, parent);
		}
		if (convertView instanceof TextView) {
			configureTextView((TextView) convertView);
		}

		return convertView;
	}

	/**
	 * Configures text view. Is called for the DEFAULT_ITEM_ID views.
	 *
	 * @param view the text view to be configured
	 */
	protected void configureTextView(TextView view) {
		if (textColor != 0) {
			view.setTextColor(textColor);
		}
		if (textSize != 0) {
			view.setTextSize(textSize);
		}
		if (typeface != null) {
			view.setTypeface(typeface);
		}
	}

	/**
	 * Loads a text view from view
	 *
	 * @param view         the text view or layout containing it
	 * @param textResource the text resource Id in layout
	 * @return the loaded text view
	 */
	private TextView getTextView(View view, int textResource) {
		TextView text;
		try {
			text = (TextView) view.findViewById(textResource);
		} catch (ClassCastException e) {
			throw new IllegalStateException(
					"AbstractWheelAdapter requires the resource ID to be a TextView", e);
		}
		return text;
	}

	/**
	 * Loads view from resources
	 *
	 * @param resource the resource Id
	 * @return the loaded view or null if resource is not saveConfig
	 */
	private View getView(int resource, ViewGroup parent) {
		return inflater.inflate(resource, parent, false);
	}
}
