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
import com.example.kreedaankana.databinding.FragmentSignupBinding

class SignupFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null
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
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSignup.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val password = binding.etPassword.text.toString()
            binding.tvError.visibility = View.GONE
            viewModel.signup(name, email, phone, password)
        }

        binding.btnGoToLogin.setOnClickListener {
            findNavController().navigate(R.id.action_signup_to_login)
        }

        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Loading -> {
                    binding.btnSignup.isEnabled = false
                    binding.btnSignup.text = "Creating account..."
                }
                is AuthState.Success -> {
                    val app = requireActivity().application as KreedaApplication
                    app.sessionManager.saveSession(
                        state.user.id,
                        state.user.name,
                        state.user.email,
                        state.user.role
                    )
                    findNavController().navigate(R.id.action_signup_to_home)
                }
                is AuthState.Error -> {
                    binding.btnSignup.isEnabled = true
                    binding.btnSignup.text = getString(R.string.signup)
                    binding.tvError.visibility = View.VISIBLE
                    binding.tvError.text = when (state.message) {
                        "Invalid name" -> getString(R.string.invalid_name)
                        "Invalid email" -> getString(R.string.invalid_email)
                        "Invalid phone" -> getString(R.string.invalid_phone)
                        "Invalid password" -> getString(R.string.invalid_password)
                        "Email already registered" -> getString(R.string.email_exists)
                        else -> state.message
                    }
                }
                else -> {
                    binding.btnSignup.isEnabled = true
                    binding.btnSignup.text = getString(R.string.signup)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
