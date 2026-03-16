import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Link } from "react-router-dom";
import { useAuth } from "@/contexts/AuthContext";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
  FormDescription,
} from "@/components/ui/form";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { UserPlus, Mail, User, Lock, Sparkles, Briefcase } from "lucide-react";
import { cn } from "@/lib/utils";

const registerSchema = z.object({
  email: z.string().email("Email must be valid").min(1, "Email is required"),
  username: z
    .string()
    .min(3, "Username must be at least 3 characters")
    .max(50, "Username must be at most 50 characters"),
  password: z.string().min(8, "Password must be at least 8 characters"),
  isCreator: z.boolean(),
  isOwner: z.boolean(),
}).refine(
  (data) => data.isCreator || data.isOwner,
  {
    message: "You must select at least one role (Creator or Owner)",
    path: ["isOwner"],
  }
);

type RegisterFormValues = z.infer<typeof registerSchema>;

export function RegisterPage() {
  const { register, isLoading } = useAuth();
  const form = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      email: "",
      username: "",
      password: "",
      isCreator: false,
      isOwner: false,
    },
  });

  const onSubmit = async (data: RegisterFormValues) => {
    try {
      await register(data);
    } catch {
      // Error already handled in useAuth
    }
  };

  const isCreator = form.watch("isCreator");
  const isOwner = form.watch("isOwner");

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-background via-muted/20 to-background p-4 py-12">
      <Card className="w-full max-w-md border-2 shadow-xl">
        <CardHeader className="space-y-3 text-center pb-6">
          <div className="mx-auto w-12 h-12 rounded-full bg-primary/10 flex items-center justify-center mb-2">
            <UserPlus className="h-6 w-6 text-primary" />
          </div>
          <CardTitle className="text-3xl font-bold">Create Account</CardTitle>
          <CardDescription className="text-base">
            Get started by creating your account
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-5" noValidate>
              <FormField
                control={form.control}
                name="email"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel className="text-sm font-medium">Email</FormLabel>
                    <FormControl>
                      <div className="relative">
                        <Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                        <Input
                          type="email"
                          placeholder="john@example.com"
                          className="pl-10 h-11"
                          {...field}
                        />
                      </div>
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="username"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel className="text-sm font-medium">Username</FormLabel>
                    <FormControl>
                      <div className="relative">
                        <User className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                        <Input
                          placeholder="john_doe"
                          className="pl-10 h-11"
                          {...field}
                        />
                      </div>
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="password"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel className="text-sm font-medium">Password</FormLabel>
                    <FormControl>
                      <div className="relative">
                        <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                        <Input
                          type="password"
                          placeholder="At least 8 characters"
                          className="pl-10 h-11"
                          {...field}
                        />
                      </div>
                    </FormControl>
                    <FormDescription className="text-xs">
                      Password must be at least 8 characters long
                    </FormDescription>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <div className="space-y-3 pt-2">
                <FormLabel className="text-sm font-medium">Select Your Role</FormLabel>
                <FormField
                  control={form.control}
                  name="isCreator"
                  render={({ field }) => (
                    <FormItem>
                      <FormControl>
                        <div
                          className={cn(
                            "flex items-start space-x-3 rounded-lg border-2 p-4 cursor-pointer transition-all",
                            field.value
                              ? "border-primary bg-primary/5"
                              : "border-border hover:border-primary/50 hover:bg-accent/50"
                          )}
                          onClick={() => field.onChange(!field.value)}
                        >
                          <div className="mt-0.5">
                            <div className={cn(
                              "w-5 h-5 rounded border-2 flex items-center justify-center transition-colors",
                              field.value
                                ? "border-primary bg-primary"
                                : "border-muted-foreground/30"
                            )}>
                              {field.value && (
                                <div className="w-2 h-2 rounded-full bg-primary-foreground" />
                              )}
                            </div>
                          </div>
                          <div className="flex-1 space-y-1">
                            <div className="flex items-center gap-2">
                              <Sparkles className="h-4 w-4 text-primary" />
                              <FormLabel className="cursor-pointer font-semibold">Creator</FormLabel>
                            </div>
                            <FormDescription className="text-xs cursor-pointer">
                              Create content and apply for campaigns
                            </FormDescription>
                          </div>
                        </div>
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name="isOwner"
                  render={({ field }) => (
                    <FormItem>
                      <FormControl>
                        <div
                          className={cn(
                            "flex items-start space-x-3 rounded-lg border-2 p-4 cursor-pointer transition-all",
                            field.value
                              ? "border-primary bg-primary/5"
                              : "border-border hover:border-primary/50 hover:bg-accent/50"
                          )}
                          onClick={() => field.onChange(!field.value)}
                        >
                          <div className="mt-0.5">
                            <div className={cn(
                              "w-5 h-5 rounded border-2 flex items-center justify-center transition-colors",
                              field.value
                                ? "border-primary bg-primary"
                                : "border-muted-foreground/30"
                            )}>
                              {field.value && (
                                <div className="w-2 h-2 rounded-full bg-primary-foreground" />
                              )}
                            </div>
                          </div>
                          <div className="flex-1 space-y-1">
                            <div className="flex items-center gap-2">
                              <Briefcase className="h-4 w-4 text-primary" />
                              <FormLabel className="cursor-pointer font-semibold">Owner</FormLabel>
                            </div>
                            <FormDescription className="text-xs cursor-pointer">
                              Create campaigns and manage contracts
                            </FormDescription>
                          </div>
                        </div>
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                {!isCreator && !isOwner && (
                  <p className="text-sm text-destructive mt-2 font-medium">
                    Please select at least one role
                  </p>
                )}
              </div>
              <Button
                type="submit"
                className="w-full h-11 text-base font-semibold mt-6"
                disabled={isLoading || (!isCreator && !isOwner)}
              >
                {isLoading ? "Creating account..." : "Create Account"}
              </Button>
              <div className="text-center text-sm">
                <span className="text-muted-foreground">Already have an account? </span>
                <Link
                  to="/login"
                  className="text-primary hover:underline font-semibold"
                >
                  Sign in
                </Link>
              </div>
            </form>
          </Form>
        </CardContent>
      </Card>
    </div>
  );
}
