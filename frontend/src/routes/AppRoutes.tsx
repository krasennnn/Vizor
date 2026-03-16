import { Routes, Route } from "react-router-dom"
import { HomePage } from "@/pages/HomePage"
import { ContractsPage } from "@/pages/ContractsPage"
import { ContractDetailPage } from "@/pages/ContractDetailPage"
import { CampaignPage } from "@/pages/CampaignPage"
import { CampaignDetailPage } from "@/pages/CampaignDetailPage"
import { CreatorAnalyticsPage } from "@/pages/CreatorAnalyticsPage"
import { VideoAnalyticsPage } from "@/pages/VideoAnalyticsPage"
import { AccountsPage } from "@/pages/AccountsPage"
import { AccountDetailPage } from "@/pages/AccountDetailPage"
import { LoginPage } from "@/pages/LoginPage"
import { RegisterPage } from "@/pages/RegisterPage"

export function AppRoutes() {
	return (
		<Routes>
			<Route path="/" element={<HomePage />} />
			<Route path="/contracts" element={<ContractsPage />} />
			<Route path="/contracts/:id" element={<ContractDetailPage />} />
			<Route path="/campaign" element={<CampaignPage />} />
			<Route path="/campaign/:id" element={<CampaignDetailPage />} />
			<Route path="/campaigns/:campaignId/creator/:contractId/analytics" element={<CreatorAnalyticsPage />} />
			<Route path="/videos/:id" element={<VideoAnalyticsPage />} />
			<Route path="/accounts" element={<AccountsPage />} />
			<Route path="/accounts/:id" element={<AccountDetailPage />} />
			<Route path="/login" element={<LoginPage />} />
			<Route path="/register" element={<RegisterPage />} />
		</Routes>
	)
}


