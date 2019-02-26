package com.dotpad.dialogs;

import java.util.Calendar;

import com.dotpad.R;
import com.dotpad.logic.RadioGroup;
import com.dotpad.logic.listeners.DotDialogListener;
import com.dotpad.model.Dot;

import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

public class DotDialog extends BaseDialog implements View.OnClickListener {

	private DotDialogListener mListener;
	private EditText mNote;
	private TextView mReminder;
	private RadioGroup mRadioSizeGroup;
	private RadioGroup mRadioColorGroup;
	private CheckBox mIsSticked;

	private DatePickerDialog mDatePickerDialog;
	private TimePickerDialog mTimePickerDialog;
	private Calendar mChoosedDate;
	private Dot mDot;
	private long mReminderTime;

	public DotDialog(Context context, DotDialogListener listener) {
		this(context, null, listener);
	}

	public DotDialog(Context context, Dot dot, DotDialogListener listener) {

		super(context);
		
		setContentView(R.layout.dialog_dot);

		mDot = dot;
		mListener = listener;
		mReminderTime = 0;
		
		mNote = (EditText)findViewById(R.id.dot_dialog_text);
		mReminder = (TextView)findViewById(R.id.dot_dialog_reminder_text);
		mRadioSizeGroup = (RadioGroup)findViewById(R.id.dot_dialog_size);
		mRadioColorGroup = (RadioGroup)findViewById(R.id.dot_dialog_color);
		mIsSticked = (CheckBox)findViewById(R.id.is_sticked);

		findViewById(R.id.dot_dialog_add).setOnClickListener(this);
		findViewById(R.id.dot_dialog_reset).setOnClickListener(this);
		findViewById(R.id.dot_dialog_reminder).setOnClickListener(this);
		
		if (dot != null) {
		
			mNote.setText(dot.text);
			mRadioSizeGroup.setCheckedValue(dot.size);
			mRadioColorGroup.setCheckedValue(dot.color);
			mIsSticked.setChecked(dot.isSticked);
			
			if (dot.reminder != 0)
				setReminder(mDot.getReminder());
		}
		else {
			showKeyboard();
			findViewById(R.id.dot_dialog_reset).setVisibility(View.GONE);
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.dot_dialog_add:
				mListener.onAdd(
						mNote.getText().toString(), 
						(int)mRadioColorGroup.getCheckedValue(),
						(int)mRadioSizeGroup.getCheckedValue(),
						mIsSticked.isChecked(),
						mReminderTime);
				break;
			case R.id.dot_dialog_reset:
				mListener.onReset(mDot);
				break;
			case R.id.dot_dialog_reminder:
				showReminderDialog();				
				return;
		}
		dismiss();
	}
	
	private void showKeyboard() {
		InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
	}

	private void showReminderDialog() {
		if (mDot != null && mDot.reminder != 0) {
			new Builder(new ContextThemeWrapper(getContext(), android.R.style.Theme_Holo_Light_Dialog))
				.setTitle(R.string.dialog_reminder_reset)
				.setPositiveButton(R.string.dialog_reminder_yes, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						resetReminder();
					}
				})
				.setNegativeButton(R.string.dialog_reminder_no, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) { }
				})
				.show();
		}
		else {
			Calendar calendar = Calendar.getInstance();
			mDatePickerDialog = new DatePickerDialog(
					new ContextThemeWrapper(getContext(), android.R.style.Theme_Holo_Light_Dialog), 
					new OnDateSetListener() {
						@Override
						public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
							if (view.isShown())
								DotDialog.this.onDateSet(year, monthOfYear, dayOfMonth);
						}
					}, 
					calendar.get(Calendar.YEAR), 
					calendar.get(Calendar.MONTH), 
					calendar.get(Calendar.DAY_OF_MONTH));
			mDatePickerDialog.setTitle(R.string.dialog_reminder_date_title);
			mDatePickerDialog.show();
		}
	}
	
	private void resetReminder() {
		mReminder.setText(R.string.dialog_reminder_empty);
		mDot.reminder = 0;
		mListener.onUpdate(mDot);
	}
	
	private void onDateSet(int year, int monthOfYear, int dayOfMonth) {
		mDatePickerDialog.dismiss();
		
		mChoosedDate = Calendar.getInstance();
		mChoosedDate.set(year, monthOfYear, dayOfMonth);
		
		Calendar calendar = Calendar.getInstance();
		
		mTimePickerDialog = new TimePickerDialog(
				getContext(), 
				android.R.style.Theme_Holo_Light_Dialog_NoActionBar, 
				new OnTimeSetListener() {
					
					@Override
					public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
						if (view.isShown())
							DotDialog.this.onTimeSet(hourOfDay, minute);
					}
				}, 
				calendar.get(Calendar.HOUR_OF_DAY), 
				calendar.get(Calendar.MINUTE), 
				true);
		
		mTimePickerDialog.setTitle(R.string.dialog_reminder_time_title);
		mTimePickerDialog.show();
	}

	private void onTimeSet(int hourOfDay, int minute) {

		mTimePickerDialog.dismiss();
		
		mChoosedDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
		mChoosedDate.set(Calendar.MINUTE, minute);
		
		setReminder(mChoosedDate);
	}
	
	private void setReminder(Calendar date) {
		final String summary = getString(
				R.string.dialog_reminder_summary, 
				date.get(Calendar.YEAR),
				date.get(Calendar.MONTH) + 1,
				date.get(Calendar.DAY_OF_MONTH),
				date.get(Calendar.HOUR_OF_DAY),
				date.get(Calendar.MINUTE));
		mReminder.setText(summary);
		mReminderTime = date.getTimeInMillis();
	}
}