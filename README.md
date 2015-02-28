# Android-WheelDateTimePicker
Android滚轮日期时间选择组件，日期支持农历

注：（项目进一步完善中，暂时不建议用于生产环境）

---

##简单实用
以日期选择为例说明（时间选择同理）：

```
		//首先创建监听器
		DatePickerFragment.OnDateSetListener listener = new DatePickerFragment.OnDateSetListener() {
			@Override
			public void onDateSet(Date date) {
				//todo 
			}

			@Override
			public void onDateCancle() {

			}
		};

		//然后builder出WheelDatePicker示例，调用show()方法即可
		WheelDatePicker datePicker = new WheelDatePicker.Builder(getSupportFragmentManager())
				.setInitialDate()
				.setListener(listener)
				.build();
		datePicker.show();

```


