package com.example.testdemo.view.bottomsheet

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.testdemo.R

/**
 * 头像选择底部弹窗
 */
class AvatarBottomSheet : BottomSheetDialogFragment(), View.OnClickListener {

    private var onTakePhotoListener: (() -> Unit)? = null
    private var onSelectFromAlbumListener: (() -> Unit)? = null

    fun setOnTakePhotoListener(listener: () -> Unit) {
        onTakePhotoListener = listener
    }

    fun setOnSelectFromAlbumListener(listener: () -> Unit) {
        onSelectFromAlbumListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_avatar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.btn_take_photo).setOnClickListener(this)
        view.findViewById<View>(R.id.btn_select_album).setOnClickListener(this)
        view.findViewById<View>(R.id.btn_cancel).setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_take_photo -> {
                onTakePhotoListener?.invoke()
                dismiss()
            }
            R.id.btn_select_album -> {
                onSelectFromAlbumListener?.invoke()
                dismiss()
            }
            R.id.btn_cancel -> {
                dismiss()
            }
        }
    }
}
