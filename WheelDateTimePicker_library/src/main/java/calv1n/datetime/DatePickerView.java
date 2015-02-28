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

import calv1n.datetime.dependency.Lunar;
import calv1n.datetime.dependency.wheelview.ArrayWheelAdapter;
import calv1n.datetime.dependency.wheelview.NumericWheelAdapter;
import calv1n.datetime.dependency.wheelview.WheelView;

/**
 * 自定义日期选择</br>
 * 对 {@link WheelView}的封装
 * Created by calvin on 2015/1/28.
 */
public class DatePickerView extends LinearLayout {

	/**
	 * 年跨度
	 */
	private final static int YEAR_SPAN = 100;

	private final static int DEFALUT_PRIMARY_TEXT_COLOR = /*R.color.text_pink*/0xffFC8A8A;
	private final static int DEFAULT_MINOR_TEXT_COLOR = /*R.color.text_gray_content*/0xff666666;
	private boolean isLunar = false;

	private Context mContext;
	private OnDateChangedListener dateChangedListener;

	private WheelView.OnWheelChangedListener listener;
	private WheelView wvMonth, wvYear, wvDay;
	private DatePickerNumericAdapter yearAdapter, dayAdapter;
	private DatePickerArrayAdapter monthAdapter;
	private int primaryTextColor;
	private int minorTextColor;

	private int year;
	private int monthOfYear;
	private int dayOfMonth;

	public DatePickerView(Context context) {
		this(context, null, 0);
	}

	public DatePickerView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DatePickerView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs);
		this.mContext = context;
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {

		Calendar cal = Calendar.getInstance();
		year = cal.get(Calendar.YEAR);
		monthOfYear = cal.get(Calendar.MONTH);
		dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

		if (null != attrs) {
			TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.DatePickerView);
			Drawable backgroundDrawable = ta.getDrawable(R.styleable.DatePickerView_wheel_background);
			Drawable centerDrawable = ta.getDrawable(R.styleable.DatePickerView_wheel_foreground);
			int itemCount = ta.getInteger(R.styleable.DatePickerView_wheel_number, 3);
			String defaultDate = ta.getString(R.styleable.DatePickerView_default_date);
			year = ta.getInteger(R.styleable.DatePickerView_year, year);
			monthOfYear = ta.getInteger(R.styleable.DatePickerView_monthOfYear, monthOfYear);
			dayOfMonth = ta.getInteger(R.styleable.DatePickerView_dayOfMonth, dayOfMonth);
			primaryTextColor = ta.getColor(R.styleable.DatePickerView_primary_textColor, DEFALUT_PRIMARY_TEXT_COLOR);
			minorTextColor = ta.getColor(R.styleable.DatePickerView_minor_textColor, DEFAULT_MINOR_TEXT_COLOR);

			ta.recycle();
		} else {
			primaryTextColor = DEFALUT_PRIMARY_TEXT_COLOR;
			minorTextColor = DEFAULT_MINOR_TEXT_COLOR;

		}

		initView();

	}

	private void initView() {

		LayoutInflater.from(getContext()).inflate(R.layout.date_picker_view, this, true);

		wvYear = (WheelView) findViewById(R.id.wheel_year);
		initWheelStyle(wvYear);
		wvMonth = (WheelView) findViewById(R.id.wheel_month);
		initWheelStyle(wvMonth);
		wvDay = (WheelView) findViewById(R.id.wheel_day);
		initWheelStyle(wvDay);

		//滚动监听器
		listener = new WheelView.OnWheelChangedListener() {
			@Override
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				//int diffValue = newValue - oldValue;
				//刷新适配器
				switch (wheel.getId()) {
					case R.id.wheel_year:
						yearAdapter.setHighlightIndex(newValue);
						updateDays(wvYear, wvMonth, wvDay, isLunar);
						break;
					case R.id.wheel_month:
						monthAdapter.setHighlightIndex(newValue);
						updateDays(wvYear, wvMonth, wvDay, isLunar);
						break;
					case R.id.wheel_day:
						dayAdapter.setHighlightIndex(newValue);
						break;
				}

				if (null != dateChangedListener) {
					dateChangedListener.onDateChanged(getDate(wvYear, wvMonth, wvDay), isLunar);
				}

			}
		};

		//为wheelView设置数据初始数据
		setWheelView(isLunar);
	}

	private void setWheelView(boolean isLunar) {

		//年
		yearAdapter = new DatePickerNumericAdapter(mContext, year - YEAR_SPAN, year + YEAR_SPAN, YEAR_SPAN, isLunar);
		wvYear.setViewAdapter(yearAdapter);
		wvYear.addChangingListener(listener);
		wvYear.setCurrentItem(YEAR_SPAN);

		//获取月份数组
		String[] hanMonths = getResources().getStringArray(R.array.months_han);
		String[] digitMonths = getResources().getStringArray(R.array.months_digit);
		monthAdapter = new DatePickerArrayAdapter(mContext, isLunar ? hanMonths : digitMonths, monthOfYear - 1);
		wvMonth.setViewAdapter(monthAdapter);
		wvMonth.addChangingListener(listener);
		wvMonth.setCurrentItem(monthOfYear - 1);

		//天
		wvDay.addChangingListener(listener);
		updateDays(wvYear, wvMonth, wvDay, isLunar);

		wvDay.setCurrentItem(dayOfMonth - 1);
	}

	private void updateDays(WheelView year, WheelView month, WheelView day, boolean isLunar) {

		Date date = getDate(year, month, day);

		if (null == date) {
			return;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);

		//当月最大值
		int maxDays = cal.getMaximum(Calendar.DAY_OF_MONTH);
		if (isLunar) {
			//农历
			Lunar lunar = new Lunar(cal.getTimeInMillis());
			//得到选中年月最大天数
			maxDays = lunar.getMaxDayInMonth();
		}
		//设置适配器
		dayAdapter = new DatePickerNumericAdapter(mContext, 1, maxDays, cal.get(Calendar.DAY_OF_MONTH), isLunar);
		day.setViewAdapter(dayAdapter);
		int curDay = Math.min(maxDays, day.getCurrentItem() + 1);
		//设置当前条目
		day.setCurrentItem(curDay - 1, true);
		dayAdapter.setHighlightIndex(curDay - 1);
	}

	private Date getDate(WheelView year, WheelView month, WheelView day) {
		Date date = null;
		try {
			int yearDiff = year.getCurrentItem() - YEAR_SPAN;
			int m = month.getCurrentItem() + 1;
			int d = day.getCurrentItem() + 1;

			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.YEAR, this.year + yearDiff);
			cal.set(Calendar.MONTH, m);
			cal.set(Calendar.DAY_OF_MONTH, d);

			date = cal.getTime();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return date;
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

	public void setCurrentDate(Date currentDate) {
		setCurrentDate(currentDate, isLunar);
	}


	public void setCurrentDate(Date currentDate, boolean isLunar) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(currentDate);

		this.year = cal.get(Calendar.YEAR);
		this.monthOfYear = cal.get(Calendar.MONTH);
		this.dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
		this.isLunar = isLunar;
		setWheelView(isLunar);
	}

	public Date getCurrentDate() {
		return getDate(wvYear, wvMonth, wvDay);
	}

	public void setDateChangedListener(OnDateChangedListener dateChangedListener) {
		this.dateChangedListener = dateChangedListener;
	}

	public void setIsLunar(boolean isLunar) {
		this.isLunar = isLunar;
		//notifyDataChanged();
		setWheelView(isLunar);
	}

	public void setShowYear(boolean isShow) {
		wvYear.setVisibility(isShow ? View.VISIBLE : View.GONE);
	}

	public void notifyDataChanged() {
		yearAdapter.notifyDataChanged();
		monthAdapter.notifyDataChanged();
		dayAdapter.notifyDataChanged();
	}

	public interface OnDateChangedListener {
		/**
		 * 点击确定后的回调方法
		 *
		 * @param newValue 该值有null情况
		 */
		public void onDateChanged(Date newValue, boolean isLunar);
	}

	/**
	 * 日期选择器中'年'和'日'的适配器
	 */
	public class DatePickerNumericAdapter extends NumericWheelAdapter {
		/**
		 * 高亮条目下标
		 */
		private int highlightIndex;
		private int middleIndex;
		int minValue;
		int maxValue;
		//是否以农历显示
		private boolean isLunar;
		private String format;

		/**
		 * 构造器
		 *
		 * @param context   上下文
		 * @param minValue  起始值
		 * @param maxValue  最大值,结束值
		 * @param initValue 起始值的下标index
		 * @param isLunar   是否以农历显示
		 */
		public DatePickerNumericAdapter(Context context, int minValue, int maxValue, int initValue, boolean isLunar) {
			super(context, minValue, maxValue);
			this.highlightIndex = initValue;
			this.minValue = minValue;
			this.maxValue = maxValue;
			this.isLunar = isLunar;

		}

		//自定义item布局
		private void setResource() {
			//设置条目
			setItemResource(R.layout.wheel_item, R.id.tv_wheel_item);
		}

		@Override
		protected void configureTextView(TextView view) {
			super.configureTextView(view);
			if (middleIndex == highlightIndex) {
				view.setTextColor(primaryTextColor);
				view.setTextSize(18);
				view.setTypeface(Typeface.DEFAULT_BOLD);
			} else {
				view.setTextColor(minorTextColor);
				view.setTextSize(16);
				view.setTypeface(Typeface.DEFAULT);
			}

		}

		@Override
		public View getItem(int index, View cachedView, ViewGroup parent) {
			this.middleIndex = index;
			return super.getItem(index, cachedView, parent);
		}

		@Override
		public CharSequence getItemText(int index) {
			if (index >= 0 && index < getItemsCount()) {
				int value = minValue + index;
				if (value > 1900) { //输入'年'适配器
					return isLunar ? Lunar.getHansYear(value) : Integer.toString(value) + "年";
				} else {  //'天'适配器
					return isLunar ? Lunar.getLunarDayString(value) : Integer.toString(value) + "日";
				}
			}
			return "";
		}

		private void setHighlightIndex(int index) {
			this.highlightIndex = index;
			notifyDataChanged();
		}

		private void notifyDataChanged() {
			notifyDataChangedEvent();
		}
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
