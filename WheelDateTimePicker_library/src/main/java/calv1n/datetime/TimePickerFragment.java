package calv1n.datetime;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import java.util.Calendar;
import java.util.Date;

/**
 * 时间选择器主键
 * Created by duchengzhen on 2014/9/10.
 * Done is better then prefect
 */
public class TimePickerFragment extends DialogFragment implements View.OnClickListener {

	//默认值
	private Integer hourOfDay;
	private Integer minute;
	private static OnTimeSetListener mListener;
	protected TimePickerView timePicker;
	private Date mInitTime;


	public TimePickerFragment() {

	}

	public static TimePickerFragment newInstance(OnTimeSetListener listener, Date initTime) {
		mListener = listener;
		Bundle b = new Bundle();
		b.putSerializable("time", initTime);
		TimePickerFragment frag = new TimePickerFragment();
		frag.setArguments(b);

		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);

		unpackBundle();
	}

	private void unpackBundle() {
		Bundle b = getArguments();

		if (null == b) {
			return;
		}

		mInitTime = (Date) b.getSerializable("time");
		initTime(mInitTime);
	}

	private void initTime(Date time) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(time);

		hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
		minute = cal.get(Calendar.MINUTE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		Dialog dialog = getDialog();
		//去掉titlebar
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//改变窗口的大小和位置
		//changeWindow(dialog);

		return inflater.inflate(R.layout.time_picker_fragment, container, true);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		//初始化wheelView
		initView(view);
	}


	private void initView(View view) {

		view.findViewById(R.id.btn_cancel).setOnClickListener(this);
		view.findViewById(R.id.btn_commit).setOnClickListener(this);

		timePicker = (TimePickerView) view.findViewById(R.id.tpv);
		if (null != hourOfDay && minute != null) {

			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.MINUTE, minute);
			cal.set(Calendar.HOUR_OF_DAY, hourOfDay);

			timePicker.setCurrentTime(cal.getTime(), false);
		}
	}


	/**
	 * 改变activity的位置和大小
	 */
	private void changeWindow(Dialog dialog) {
		Window window = dialog.getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		window.setGravity(Gravity.BOTTOM);
		lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
		lp.height = ((int) getResources().getDimension(R.dimen.calendar_popup_height));
		window.setAttributes(lp);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_cancel:
				cancel();
				break;
			case R.id.btn_commit:
				commit();
				break;
		}
	}

	//取消
	public void cancel() {
		if (null != mListener) {
			mListener.onTimeCancle();
		}
		dismiss();
	}

	//确定
	public void commit() {
		Date time = timePicker.getCurrentTime();
		if (null != mListener) {
			mListener.onTimeSet(time);
		}

		dismiss();
	}


	public void setOnTimeSetListener(OnTimeSetListener onTimeSetListener) {
		mListener = onTimeSetListener;
	}

	public interface OnTimeSetListener {
		/**
		 * 点击确定后的回调方法
		 *
		 * @param time 选中的时间
		 */
		public void onTimeSet(Date time);

		public void onTimeCancle();

	}

}
