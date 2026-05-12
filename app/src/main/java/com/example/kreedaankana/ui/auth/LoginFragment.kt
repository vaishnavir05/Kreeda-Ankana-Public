package com.example.kreedaankana.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.kreedaankana.KreedaApplication
import com.example.kreedaankana.R
import com.example.kreedaankana.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = requireActivity().application as KreedaApplication
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(app.userRepository) as T
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            binding.tvError.visibility = View.GONE
            viewModel.login(email, password)
        }

        binding.btnGoToSignup.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_signup)
        }

        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Loading -> {
                    binding.btnLogin.isEnabled = false
                    binding.btnLogin.text = "Logging in..."
                }
                is AuthState.Success -> {
                    val app = requireActivity().application as KreedaApplication
                    app.sessionManager.saveSession(
                        state.user.id,
                        state.user.name,
                        state.user.email,
                        state.user.role
                    )
                    findNavController().navigate(R.id.action_login_to_home)
                }
                is AuthState.Error -> {
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.text = getString(R.string.login)
                    binding.tvError.visibility = View.VISIBLE
                    binding.tvError.text = state.message
                }
                else -> {
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.text = getString(R.string.login)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
