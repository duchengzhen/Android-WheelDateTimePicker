package calv1n.datetime;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;

import calv1n.datetime.dependency.wheelview.ArrayWheelAdapter;
import calv1n.datetime.dependency.wheelview.WheelView;

/**
 * 自定义日期选择</br>
 * 对 {@link WheelView}的封装
 * Created by calvin on 2015/1/28.
 */
public class TimePickerView extends LinearLayout {

	private final static int DEFALUT_PRIMARY_TEXT_COLOR = /*R.color.text_pink*/0xffFC8A8A;
	private final static int DEFAULT_MINOR_TEXT_COLOR = /*R.color.text_gray_content*/0xff666666;

	private Context mContext;
	private OnTimeChangedListener dateChangedListener;

	private WheelView.OnWheelChangedListener listener;
	private WheelView wvHour, wvMin;
	private DatePickerArrayAdapter hourAdapter, minAdapter;
	private int primaryTextColor;
	private int minorTextColor;

	private int hourOfDay;
	private int minOfHour;

	/**
	 * 是否为24小时制
	 */
	private boolean is12Hour;

	public TimePickerView(Context context) {
		this(context, null, 0);
	}

	public TimePickerView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TimePickerView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs);
		this.mContext = context;
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {

		Calendar cal=Calendar.getInstance();
		hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
		minOfHour = cal.get(Calendar.MINUTE);

		if (null != attrs) {
			TypedArray timeTa = context.obtainStyledAttributes(attrs, R.styleable.TimePickerView);
			TypedArray dataTa = context.obtainStyledAttributes(attrs, R.styleable.DatePickerView);

			Drawable backgroundDrawable = dataTa.getDrawable(R.styleable.DatePickerView_wheel_background);
			Drawable centerDrawable = dataTa.getDrawable(R.styleable.DatePickerView_wheel_foreground);
			int itemCount = dataTa.getInteger(R.styleable.DatePickerView_wheel_number, 3);
			String defaultDate = dataTa.getString(R.styleable.DatePickerView_default_date);

			primaryTextColor = dataTa.getColor(R.styleable.DatePickerView_primary_textColor, DEFALUT_PRIMARY_TEXT_COLOR);
			minorTextColor = dataTa.getColor(R.styleable.DatePickerView_minor_textColor, DEFAULT_MINOR_TEXT_COLOR);

			int timeStyle = timeTa.getInteger(R.styleable.TimePickerView_time_style, 1);
			is12Hour = timeStyle == 0;

			hourOfDay = timeTa.getInteger(R.styleable.TimePickerView_hour, hourOfDay);
			minOfHour = timeTa.getInteger(R.styleable.TimePickerView_minute, minOfHour);

			timeTa.recycle();
			dataTa.recycle();
		} else {
			primaryTextColor = DEFALUT_PRIMARY_TEXT_COLOR;
			minorTextColor = DEFAULT_MINOR_TEXT_COLOR;
		}

		initView();

	}

	private void initView() {

		LayoutInflater.from(getContext()).inflate(R.layout.time_picker_view, this, true);

		wvMin = (WheelView) findViewById(R.id.wheel_minute);
		initWheelStyle(wvMin);
		wvHour = (WheelView) findViewById(R.id.wheel_hour);
		initWheelStyle(wvHour);

		//滚动监听器
		listener = new WheelView.OnWheelChangedListener() {
			@Override
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				//int diffValue = newValue - oldValue;
				//刷新适配器
				switch (wheel.getId()) {
					case R.id.wheel_hour:
						hourAdapter.setHighlightIndex(newValue);
						break;
					case R.id.wheel_minute:
						minAdapter.setHighlightIndex(newValue);
						break;
				}
				if (null != dateChangedListener) {
					dateChangedListener.onTimeChanged(getDate(wvHour, wvMin), is12Hour);
				}
			}
		};

		//为wheelView设置数据初始数据
		setWheelView(is12Hour);
	}

	private void setWheelView(boolean is12HourTime) {

		String[] minStr = getResources().getStringArray(R.array.minute);
		minAdapter = new DatePickerArrayAdapter(mContext, minStr, minOfHour);

		wvMin.setViewAdapter(minAdapter);
		wvMin.addChangingListener(listener);
		wvMin.setCurrentItem(minOfHour);

		//获取月份数组
		String[] hourStr = getResources().getStringArray(R.array.hour);
		//小时
		hourAdapter = new DatePickerArrayAdapter(mContext, hourStr, hourOfDay);
		wvHour.setViewAdapter(hourAdapter);
		wvHour.addChangingListener(listener);
		wvHour.setCurrentItem(hourOfDay);
	}

	private Date getDate(WheelView hour,WheelView min){
		Calendar cal=Calendar.getInstance();
		int hourOfDay = hour.getCurrentItem();
		int minOfHour=min.getCurrentItem();

		cal.set(Calendar.HOUR_OF_DAY,hourOfDay);
		cal.set(Calendar.MINUTE,minOfHour);

		return cal.getTime();
	}
	/**
	 * 初始化样式
	 */
	private void initWheelStyle(WheelView wheel) {
		//无渐变色(透明)
		//wheel.setShadowColor(Color.TRANSPARENT,Color.TRANSPARENT,Color.TRANSPARENT);
		wheel.setDrawShadows(false);
		//无前景
		wheel.setDrawForground(false);
		//wheel.setWheelBackground(R.drawable.transparent);
		//wheel.setWheelCenter(R.drawable.wheel_default_forground);
		//设置显示条目
		wheel.setVisibleItems(3);
		//设置循环滚动
		wheel.setCyclic(true);
	}

	public void setCurrentTime(Date currentTime, boolean is12HourTime){
		Calendar cal = Calendar.getInstance();
		cal.setTime(currentTime);

		this.hourOfDay=cal.get(Calendar.HOUR_OF_DAY);
		this.minOfHour=cal.get(Calendar.MINUTE);
		setWheelView(is12HourTime);
	}

	public Date getCurrentTime() {
		return getDate(wvHour, wvMin);
	}

	public void setDateChangedListener(OnTimeChangedListener dateChangedListener) {
		this.dateChangedListener = dateChangedListener;
	}

	public void setIs12HourTime(boolean hour_12) {
		this.is12Hour = hour_12;
		setWheelView(hour_12);
	}

	public void setShowYear(boolean isShow) {
		wvMin.setVisibility(isShow ? View.VISIBLE : View.GONE);
	}

	public void notifyDataChanged() {
		hourAdapter.notifyDataChanged();
		minAdapter.notifyDataChanged();
	}

	public interface OnTimeChangedListener {
		/**
		 * 点击确定后的回调方法
		 *
		 * @param newValue 该值有null情况
		 */
		public void onTimeChanged(Date newValue, boolean is12HourTime);
	}


	private class DatePickerArrayAdapter extends ArrayWheelAdapter<String> {
		// Index of current item
		int middleIndex;
		// Index of item to be highlighted
		int highlightIndex;

		/**
		 * Constructor
		 */
		public DatePickerArrayAdapter(Context context, String[] items, int initIndex) {
			super(context, items);
			this.highlightIndex = initIndex;
		}

		@Override
		protected void configureTextView(TextView view) {
			super.configureTextView(view);
			if (middleIndex == highlightIndex) {
				view.setTypeface(Typeface.DEFAULT_BOLD);
				view.setTextSize(18);
				view.setTextColor(primaryTextColor);
			} else {
				view.setTypeface(Typeface.DEFAULT);
				view.setTextSize(16);
				view.setTextColor(minorTextColor);

			}

		}

		@Override
		public View getItem(int index, View cachedView, ViewGroup parent) {
			middleIndex = index;
			return super.getItem(index, cachedView, parent);
		}

		public void setHighlightIndex(int index) {
			this.highlightIndex = index;
			notifyDataChanged();
		}

		private void notifyDataChanged() {
			notifyDataChangedEvent();
		}
	}

	public void setPrimaryTextColor(int primaryTextColor) {
		this.primaryTextColor = primaryTextColor;
	}

	public void setMinorTextColor(int minorTextColor) {
		this.minorTextColor = minorTextColor;
	}
}
