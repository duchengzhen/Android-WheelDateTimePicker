package calv1n.datetime;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import java.util.Calendar;
import java.util.Date;


/**
 * 日期选择组件
 * <br/>Created by duchengzhen on 14/11/17.<br/>
 * Done is better then prefect.</br>
 */
public class DatePickerFragment extends DialogFragment implements View.OnClickListener {

	public static final String TAG_DATE_PICKER_FRAGMENT="datepickerfragment";

	private static OnDateSetListener onDateSetListener = null;

	//默认值
	private Integer year;
	private Integer month;
	private Integer day;
	private DatePickerView datePicker;
	private Date mInitDate;


	public static DatePickerFragment newInstance(OnDateSetListener listener,Date initDate) {
		onDateSetListener=listener;
		//
		Bundle bundle=new Bundle();
		bundle.putSerializable("data",initDate);
		DatePickerFragment fragment = new DatePickerFragment();
		fragment.setArguments(bundle);

		return fragment;
	}

	public DatePickerFragment() {

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);

		unpackBundle();
	}

	private void unpackBundle(){
		Bundle b = getArguments();

		if (null==b){
			return;
		}

		mInitDate= (Date) b.getSerializable("date");
		initDate(mInitDate);
	}

	private void initDate (Date date) {
		Calendar cal=Calendar.getInstance();
		cal.setTime(date);

		this.year = cal.get(Calendar.YEAR);
		this.month = cal.get(Calendar.MONTH);
		this.day = cal.get(Calendar.DAY_OF_MONTH);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		removeOriginalTitlebar();
		return inflater.inflate(R.layout.date_picker_fragment, container, true);
	}

	private void removeOriginalTitlebar(){
		Dialog dialog = getDialog();
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
	}
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		initView(view);

		if (null != year && null != month && null != day) {
			Calendar cal=Calendar.getInstance();
			cal.set(year,month,day);

			datePicker.setCurrentDate(cal.getTime());
		}
	}

	private void initView(View view) {

		view.findViewById(R.id.btn_cancel).setOnClickListener(this);
		view.findViewById(R.id.btn_commit).setOnClickListener(this);

		datePicker = (DatePickerView) view.findViewById(R.id.dpv);

	}

	@Override
	public void onDestroyView() {
		if (null!=getDialog()&&getRetainInstance()){
			getDialog().setDismissMessage(null);
		}
		super.onDestroyView();
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
		if (null!=onDateSetListener){
			onDateSetListener.onDateCancle();
		}
		dismiss();
	}

	//确定
	public void commit() {

		if (null != onDateSetListener) {
			onDateSetListener.onDateSet(datePicker.getCurrentDate());
		}

		dismiss();
	}

	public interface OnDateSetListener {
		/**
		 * 点击确定后的回调方法
		 *
		 * @param date  选中的年
		 */
		public void onDateSet(Date date);

		/**
		 * 点击取消按钮的回调方法
		 */
		public void onDateCancle();
	}
}
