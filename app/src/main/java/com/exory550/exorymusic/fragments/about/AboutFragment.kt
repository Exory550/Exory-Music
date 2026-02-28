package com.exory550.exorymusic.fragments.about

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.exory550.exorymusic.Constants
import com.exory550.exorymusic.R
import com.exory550.exorymusic.databinding.FragmentAboutBinding
import com.exory550.exorymusic.extensions.openUrl
import com.exory550.exorymusic.util.NavigationUtil
import dev.chrisbanes.insetter.applyInsetter

class AboutFragment : Fragment(R.layout.fragment_about), View.OnClickListener {
    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAboutBinding.bind(view)
        binding.aboutContent.cardExoryInfo.version.setSummary(getAppVersion())
        setUpView()

        binding.aboutContent.root.applyInsetter {
            type(navigationBars = true) {
                padding(vertical = true)
            }
        }
    }

    private fun setUpView() {
        binding.aboutContent.cardExoryInfo.appGithub.setOnClickListener(this)
        binding.aboutContent.cardExoryInfo.faqLink.setOnClickListener(this)
        binding.aboutContent.cardExoryInfo.appTranslation.setOnClickListener(this)
        binding.aboutContent.cardExoryInfo.bugReportLink.setOnClickListener(this)
        binding.aboutContent.cardExoryInfo.changelog.setOnClickListener(this)
        binding.aboutContent.cardExoryInfo.openSource.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.faqLink -> openUrl(Constants.FAQ_LINK)
            R.id.appGithub -> openUrl(Constants.GITHUB_PROJECT)
            R.id.appTranslation -> openUrl(Constants.TRANSLATE)
            R.id.changelog -> NavigationUtil.gotoWhatNews(requireActivity())
            R.id.openSource -> NavigationUtil.goToOpenSource(requireActivity())
            R.id.bugReportLink -> NavigationUtil.bugReport(requireActivity())
        }
    }

    private fun getAppVersion(): String {
        return try {
            requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            "0.0.0"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
